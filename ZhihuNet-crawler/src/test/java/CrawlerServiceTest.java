import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.activity.ActivityDTO;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.contract.service.CrawlerService;
import org.acpasser.zhihunet.crawler.ZhihuNetCrawlerApplication;
import org.acpasser.zhihunet.crawler.dto.ZhihuUserActivityParserDTO;
import org.acpasser.zhihunet.crawler.enums.TargetType;
import org.acpasser.zhihunet.crawler.util.JsonUtils;
import org.acpasser.zhihunet.crawler.util.ParseUtil;
import org.acpasser.zhihunet.model.ZhihuAnswer;
import org.acpasser.zhihunet.model.ZhihuArticle;
import org.acpasser.zhihunet.model.ZhihuCollection;
import org.acpasser.zhihunet.model.ZhihuPin;
import org.acpasser.zhihunet.model.ZhihuQuestion;
import org.acpasser.zhihunet.model.ZhihuTopic;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.acpasser.zhihunet.model.ZhihuUserInteraction;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuAnswerRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuArticleRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuCollectionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuPinRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuQuestionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuTopicRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserInteractionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ZhihuNetCrawlerApplication.class)
@Slf4j
public class CrawlerServiceTest {

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private ZhihuUserRepository zhihuUserRepository;

    @Autowired
    private ZhihuUserInteractionRepository interactionRepository;

    @Autowired
    private ZhihuQuestionRepository questionRepository;

    @Autowired
    private ZhihuAnswerRepository answerRepository;

    @Autowired
    private ZhihuArticleRepository articleRepository;

    @Autowired
    private ZhihuPinRepository pinRepository;

    @Autowired
    private ZhihuTopicRepository topicRepository;
    @Autowired
    private ZhihuUserInteractionRepository zhihuUserInteractionRepository;
    @Autowired
    private ZhihuCollectionRepository collectionRepository;

    @Test
    public void testParseDesc() {
        String desc = "ABC<a href=\"https://link.zhihu.com/?target=https%3A//xpqiu.github.io/\" class=\" external\" target=\"_blank\" rel=\"nofollow noreferrer\"><span class=\"invisible\">https://</span><span class=\"visible\">xpqiu.github.io/</span><span class=\"invisible\"></span></a>DEF";
        System.out.println(ParseUtil.parseDesc(desc));
    }

    @Test
    public void testBatchCrawlUserInfos1() {
        List<String> tokens = new ArrayList<>();
        tokens.add("liuxiaoran-34");
        tokens.add("openlmlab");
        tokens.add("shu-ju-guan-82");
        tokens.add("huang-rong-da-59");
        tokens.add("lei-jian-ping-93");

        UserInfoCrawlRequest request = new UserInfoCrawlRequest();
        request.setTokens(tokens);
        BaseResponse<UserCrawlResult> crawlResponse = crawlerService.batchCrawlUsers(request);
        assertTrue(crawlResponse.isSuccess());
        assertEquals(tokens.size(), crawlResponse.getResult().getUserDTOMap().size());
    }

    @Test
    public void testBatchCrawlUserInfos2() {
        List<String> tokens = new ArrayList<>();
        tokens.add("maxingjun");
        tokens.add("jasdhjkashd4"); // 不存在的用户

        UserInfoCrawlRequest request = new UserInfoCrawlRequest();
        request.setTokens(tokens);
        BaseResponse<UserCrawlResult> crawlResponse = crawlerService.batchCrawlUsers(request);
        assertTrue(crawlResponse.isSuccess());
        assertEquals(1, crawlResponse.getResult().getUserDTOMap().size());
        assertEquals(1, crawlResponse.getResult().getFailedTokenMap().size());
    }

    @Test
    public void testDeleteNonExistZhihuUsers() {
        int affectedRows = zhihuUserRepository.deleteNonExistUsers();
        System.out.println("更新了 " + affectedRows + " 条记录");
    }

    @Test
    public void testActivityDeserialize1() {
        //【类型】知乎回答
//        String answerActJson = readFile("activity_answer.json");
//        ZhihuUserActivityParserDTO ansActParserDTO = JsonUtils.fromJson(answerActJson, ZhihuUserActivityParserDTO.class);
//        System.out.println(ansActParserDTO);
//
        //【类型】知乎问题
//        String questionActJson = readFile("activity_question.json");
//        ZhihuUserActivityParserDTO queActParserDTO = JsonUtils.fromJson(questionActJson, ZhihuUserActivityParserDTO.class);
//        System.out.println(queActParserDTO);

        //【类型】知乎文章
//        String articleJson = readFile("activity_article.json");
//        ZhihuUserActivityParserDTO artActParserDTO = JsonUtils.fromJson(articleJson, new TypeReference<>() {
//        });
//        System.out.println(artActParserDTO);

        //【类型】知乎想法
//        String pinJson = readFile("activity_pin.json");
//        ZhihuUserActivityParserDTO pinActParserDTO = JsonUtils.fromJson(pinJson, new TypeReference<>() {
//        });
//        System.out.println(pinActParserDTO);

        //【类型】知乎收藏
        String collectionJson = readFile("activity_collection.json");
        ZhihuUserActivityParserDTO colActParserDTO = JsonUtils.fromJson(collectionJson, new TypeReference<>() {
        });
        System.out.println(colActParserDTO);


        // 混合4种类型
//        String actionJson = readFile("activity4.json");
//        List<ZhihuUserActivityParserDTO> activityParserDTOList = JsonUtils.fromJson(actionJson, new TypeReference<>() {
//        });
//        System.out.println(activityParserDTOList);
    }

    @Test
    public void testActivityDeserialize2() {
        List<ZhihuUserInteraction> interactions = new ArrayList<>();
        List<ZhihuQuestion> questions = new ArrayList<>();
        List<ZhihuAnswer> answers = new ArrayList<>();
        List<ZhihuArticle> articles = new ArrayList<>();
        List<ZhihuPin> pins = new ArrayList<>();
        List<ZhihuTopic> topics = new ArrayList<>();
        List<ZhihuCollection> collections = new ArrayList<>();
        // 真实case
//        String actJson = readFile("activity_zi-you-zhi-yi-21-60.json");
//        String actJson = readFile("activity_liuxiaoran-34.json");
        String actJson = readFile("activity_cong.json");
        Map<String, Object> map = JsonUtils.jsonToMap(actJson);
        String data = JsonUtils.toJson(map.get("data"));
        List<ZhihuUserActivityParserDTO> activityParserDTOList = JsonUtils.fromJson(data, new TypeReference<>() {});
        if (activityParserDTOList == null) {
            throw new BusinessException(String.format("JSON转换为List<ZhihuUserActivityParserDTO>失败，原始数据: %s", data));
        } else if (activityParserDTOList.isEmpty()) {
            log.warn("用户无任何动态...");
        }
        for (ZhihuUserActivityParserDTO activityParserDTO : activityParserDTOList) {
            // 获取ZhihuUserInteraction
            ZhihuUserInteraction interaction = new ZhihuUserInteraction();
            BeanUtils.copyProperties(activityParserDTO.getInteractionDTO(), interaction);
            interactions.add(interaction);
            // 获取Target
            switch (activityParserDTO.getTarget().getType()) {
                case ANSWER -> {
                    ZhihuAnswer answer = new ZhihuAnswer();
                    BeanUtils.copyProperties(activityParserDTO.getAnswerDTO(), answer);
                    answers.add(answer);

                    ZhihuQuestion question = new ZhihuQuestion();
                    BeanUtils.copyProperties(activityParserDTO.getQuestionDTO(), question);
                    questions.add(question);
                }
                case QUESTION -> {
                    ZhihuQuestion question = new ZhihuQuestion();
                    BeanUtils.copyProperties(activityParserDTO.getQuestionDTO(), question);
                    questions.add(question);
                }
                case ARTICLE -> {
                    ZhihuArticle article = new ZhihuArticle();
                    BeanUtils.copyProperties(activityParserDTO.getArticleDTO(), article);
                    articles.add(article);
                }
                case PIN -> {
                    ZhihuPin pin = new ZhihuPin();
                    BeanUtils.copyProperties(activityParserDTO.getPinDTO(), pin);
                    pins.add(pin);

                    activityParserDTO.getTopicDTOS().forEach(t -> {
                        ZhihuTopic topic = new ZhihuTopic();
                        BeanUtils.copyProperties(t, topic);
                        topics.add(topic);
                    });
                }
                case COLLECTION -> {
                    ZhihuCollection collection = new ZhihuCollection();
                    BeanUtils.copyProperties(activityParserDTO.getCollectionDTO(), collection);
                    collections.add(collection);
                }
                default -> throw new IllegalArgumentException("未知的target类型：" + activityParserDTO.getTarget().getType());
            }
        }
//        ParseUtil.batchSaveWithLog("interaction", interactions, interactionRepository::batchUpsert);
//        ParseUtil.batchSaveWithLog(TargetType.QUESTION.getType(), questions, questionRepository::batchUpsert);
//        ParseUtil.batchSaveWithLog(TargetType.ANSWER.getType(), answers, answerRepository::batchUpsert);
//        ParseUtil.batchSaveWithLog(TargetType.ARTICLE.getType(), articles, articleRepository::batchUpsert);
//        ParseUtil.batchSaveWithLog(TargetType.PIN.getType(), pins, pinRepository::batchUpsert);
//        ParseUtil.batchSaveWithLog("topic", topics, topicRepository::batchUpsert);
        ParseUtil.batchSaveWithLog(TargetType.COLLECTION.getType(), collections, collectionRepository::batchUpsert);
    }

    private String readFile(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining());
    }

    @Test
    public void testBatchCrawlRecentActivityByLimit() {
        List<String> tokens = new ArrayList<>();
        tokens.add("hou-de-61-48");
        tokens.add("jasdhjkashd4"); // 不会校验用户是否存在，返回0条动态
        int limit = 1;  // 爬取页数，1页是7条
        ActivityCrawlRequest request = ActivityCrawlRequest.builder()
            .tokens(tokens)
            .limit(limit)
            .build();
        BaseResponse<ActivityCrawlResult> response = crawlerService.batchCrawlRecentActivity(request);
        assertTrue(response.isSuccess());
        Map<String, ActivityDTO> activityDTOMap = response.getResult().getActivityDTOMap();
        assertEquals(2, activityDTOMap.size());
        assertEquals(limit * 7, activityDTOMap.get("hou-de-61-48").getInteractionDTOS().size());
    }

    @Test
    public void testBatchCrawlRecentActivityByDuration() {
        List<String> tokens = new ArrayList<>();
        tokens.add("xpqiu");
        ActivityCrawlRequest request = ActivityCrawlRequest.builder()
            .tokens(tokens)
            .duration(365)
            .timeUnit(TimeUnit.DAYS)
            .build();
        BaseResponse<ActivityCrawlResult> response = crawlerService.batchCrawlRecentActivity(request);
        assertTrue(response.isSuccess());
        Map<String, ActivityDTO> activityDTOMap = response.getResult().getActivityDTOMap();
        assertEquals(tokens.size(), activityDTOMap.size());
    }

    @Test
    public void testBatchCrawlRecentActivityByDuration2() {
        List<String> allTokens = zhihuUserRepository.listAll().stream()
            .map(ZhihuUser::getToken)
            .limit(10)
            .collect(Collectors.toList());
        ActivityCrawlRequest request = ActivityCrawlRequest.builder()
            .tokens(allTokens)
            .duration(365)
            .timeUnit(TimeUnit.DAYS)
            .build();
        BaseResponse<ActivityCrawlResult> response = crawlerService.batchCrawlRecentActivity(request);
        assertTrue(response.isSuccess());
    }
}