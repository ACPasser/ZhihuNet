package org.acpasser.zhihunet.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.constant.Constant;
import org.acpasser.zhihunet.common.dto.user.UserDTO;
import org.acpasser.zhihunet.common.enums.ErrorCode;
import org.acpasser.zhihunet.common.enums.UserType;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.common.request.user.UserInfoUpdateRequest;
import org.acpasser.zhihunet.common.request.user.UserRegisterRequest;
import org.acpasser.zhihunet.common.utils.JwtUtil;
import org.acpasser.zhihunet.common.utils.MD5Util;
import org.acpasser.zhihunet.core.service.UserService;
import org.acpasser.zhihunet.model.User;
import org.acpasser.zhihunet.repository.mybatis.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisTemplate<String, String> redis;

    @Value("${avatar.upload-path}")
    private String uploadPath;
    @Value("${avatar.access-prefix}")
    private String accessPrefix;

    @Override
    public String register(UserRegisterRequest userRegisterRequest) {
        String userName = userRegisterRequest.getUsername();
        if (userRepository.countByName(userName) > 0) {
            throw new BusinessException(String.format("用户名 %s 已存在", userName),
                    ErrorCode.BUSSINESS_EXCEPTION.getCode());
        }
        User user = new User();
        user.setUserName(userName);
        user.setPassword(MD5Util.encrypt(userRegisterRequest.getPassword()));
        user.setUserType(UserType.USER.name());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setDeletedAt(0);
        int ret = userRepository.insert(user);
        if (ret == 0) {
            log.info("数据库插入失败，导致用户注册失败：{}", userName);
            throw new BusinessException("用户注册失败", ErrorCode.BUSSINESS_EXCEPTION.getCode());
        }
        return "用户注册成功";
    }

    @Override
    public String login(UserRegisterRequest userRegisterRequest) {
        String userName = userRegisterRequest.getUsername();
        String inputPwd = userRegisterRequest.getPassword();
        User user = userRepository.getByName(userName);
        if (user == null) {
            throw new BusinessException(String.format("用户名 %s 不存在", userName),
                    ErrorCode.BUSSINESS_EXCEPTION.getCode());
        }
        if (!MD5Util.decrypt(inputPwd, user.getPassword())) {
            throw new BusinessException("密码错误", ErrorCode.BUSSINESS_EXCEPTION.getCode());
        }
        String redisUserKey = String.format(Constant.REDIS_USER_KEY, userName);
        if (redis.hasKey(redisUserKey)) {
            redis.delete(redisUserKey);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getUserName());
        map.put("type", user.getUserType());
        String token = JwtUtil.genJwtToken(map);
        redis.opsForValue().set(redisUserKey, token, Constant.REDIS_USER_KEY_EXPIRATION_HOUR, TimeUnit.HOURS);
        return token;
    }

    @Override
    public UserDTO getUserByName(String userName) {
        User user = userRepository.getByName(userName);
        return UserDTO.convert(user);
    }

    @Override
    public void update(UserDTO userDTO, UserInfoUpdateRequest updateRequest) {
        User user = userDTO.toUser();
        boolean hasChange = false;
        String newEmail = updateRequest.getEmail();
        if (!StringUtils.isEmpty(newEmail) && !newEmail.equals(user.getEmail())) {
            // 额外校验邮箱格式（虽然请求体已有@Email注解，但双重校验更安全）
            if (!isValidEmail(newEmail)) {
                throw new BusinessException("邮箱格式不正确");
            }
            user.setEmail(newEmail);
            hasChange = true;
        }
        String oldPwd = updateRequest.getOldPassword();
        String newPwd = updateRequest.getNewPassword();
        if (!StringUtils.isBlank(oldPwd) || !StringUtils.isEmpty(newPwd)) {
            if (StringUtils.isBlank(oldPwd)) {
                throw new BusinessException("旧密码不能为空");
            }
            if (StringUtils.isBlank(newPwd)) {
                throw new BusinessException("新密码不能为空");
            }
            if (oldPwd.length() < 5 || oldPwd.length() > 16) {
                throw new BusinessException("旧密码长度必须在5-16个字符之间");
            }
            if (newPwd.length() < 5 || newPwd.length() > 16) {
                throw new BusinessException("新密码长度必须在5-16个字符之间");
            }
            // 密码通过查数据库获取
            String password = userRepository.getById(user.getId()).getPassword();
            if (!MD5Util.decrypt(oldPwd, password)) {
                throw new BusinessException("密码不正确");
            }
            if (MD5Util.decrypt(newPwd, password)) {
                throw new BusinessException("新密码不能和旧密码相同");
            }
            user.setPassword(MD5Util.encrypt(newPwd));
            hasChange = true;
        }
        if (hasChange) {
            userRepository.update(user);
        }
    }

    // 校验邮箱格式
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(regex);
    }

    @Override
    @Transactional
    public String uploadAvatar(UserDTO userDTO, MultipartFile file) {
        String fileSuffix = checkAndGetFileSuffix(file);
        String uniqueFileName = userDTO.getUserName() + fileSuffix; // 用户名保证唯一
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // 递归创建目录
        }
        File destFile = Paths.get(uploadPath, uniqueFileName).toFile();
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            String errorMsg = String.format("文件保存失败：%s（路径：%s）",
                    e.getMessage(), destFile.getAbsolutePath());
            throw new RuntimeException(errorMsg, e);
        }
        String imageUrl = accessPrefix + uniqueFileName;
        User user = userDTO.toUser();
        user.setAvatarUrl(imageUrl);
        userRepository.update(user);
        return imageUrl;
    }

    private static String checkAndGetFileSuffix(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传头像不能为空！");
        }
        // 校验文件格式（仅允许 JPG/PNG/WEBP）
        String originalFilename = file.getOriginalFilename();
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!".jpg".equalsIgnoreCase(fileSuffix)
                && !".png".equalsIgnoreCase(fileSuffix)
                && !".webp".equalsIgnoreCase(fileSuffix)
                && !".jpeg".equalsIgnoreCase(fileSuffix)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 格式的图片！");
        }
        return fileSuffix;
    }
}
