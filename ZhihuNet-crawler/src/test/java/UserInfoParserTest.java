import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.contract.dto.user.UserCrawlResult;
import org.acpasser.zhihunet.contract.request.UserInfoCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.crawler.config.CrawlerConfigProperties;
import org.acpasser.zhihunet.crawler.parser.UserInfoParser;
import org.acpasser.zhihunet.crawler.util.HttpClientUtil;
import org.acpasser.zhihunet.crawler.util.ParseUtil;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserRepository;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserInfoParserTest {

    @InjectMocks
    private UserInfoParser userInfoParser = new UserInfoParser();
    @Mock
    private HttpClientUtil httpClientUtil;
    @Mock
    private CrawlerConfigProperties config;
    @Mock
    private ZhihuUserRepository zhihuUserRepository;

    private static final int MOCK_BATCH_SIZE = 1;
    private static final int MOCK_THREAD_COUNT = 1;
    private static final String MOCK_TOKEN = "xpqiu";
    private static final String MOCK_INVALID_TOKEN = "invalid";

    private String readFile(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new BusinessException("测试文件不存在: " + path);
        }
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining());
    }

    @BeforeEach
    public void setUp() {
        // ========== Mock 配置参数（通用逻辑） ==========
        when(config.getBatchSize()).thenReturn(MOCK_BATCH_SIZE);
        when(config.getThreadCount()).thenReturn(MOCK_THREAD_COUNT);

        userInfoParser.init();
    }

    private void mockHttpResponse(String token) {
        // 1. 构建 Mock 的 JSoup 相关对象（链式调用）
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class, RETURNS_DEEP_STUBS);

        // 2. 匹配 ZhihuRequest - URL包含目标token
        when(httpClientUtil.doGetWithRetry(
            argThat(request -> request != null && request.getUrl().contains(token))
        )).thenReturn(mockDoc);

        // 3. 模拟 DOM 解析逻辑
        when(mockDoc.select("script[id=js-initialData]")).thenReturn(mockElements);
        when(mockElements.isEmpty()).thenReturn(false);
        String jsonFilePath = String.format("user_%s.json", token);
        when(mockElements.getFirst().html()).thenReturn(readFile(jsonFilePath));
    }

    private void mockCookieExpired() {
        // 1. 构建 Mock 的 JSoup 相关对象（链式调用）
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class, RETURNS_DEEP_STUBS);

        // 2. 匹配 ZhihuRequest - URL包含目标token
        when(httpClientUtil.doGetWithRetry(any())).thenReturn(mockDoc);

        // 3. 模拟 DOM 解析逻辑
        when(mockDoc.select("script[id=js-initialData]")).thenReturn(mockElements);
        when(mockElements.isEmpty()).thenReturn(true);
    }

    @Test
    public void testParseDesc() {
        String desc = "ABC<a href=\"https://link.zhihu.com/?target=https%3A//xpqiu.github.io/\" class=\" external\" target=\"_blank\" rel=\"nofollow noreferrer\"><span class=\"invisible\">https://</span><span class=\"visible\">xpqiu.github.io/</span><span class=\"invisible\"></span></a>DEF";
        String actual = ParseUtil.parseDesc(desc);
        assertEquals("ABChttps://xpqiu.github.io/DEF", actual);
    }

    @Test
    public void testCookieExpired() {
        mockCookieExpired();

        UserInfoCrawlRequest request = UserInfoCrawlRequest.builder()
            .tokens(List.of(MOCK_TOKEN))
            .build();
        BaseResponse<UserCrawlResult> crawlResponse = userInfoParser.batchCrawl(request);
        assertTrue(crawlResponse.isSuccess());
        assertEquals(1, crawlResponse.getResult().getFailedTokenMap().size());
        assertEquals("Cookie已过期", crawlResponse.getResult().getFailedTokenMap().get(MOCK_TOKEN));
    }

    @Test
    public void testBatchCrawlUserInfos() {
        mockHttpResponse(MOCK_TOKEN);           // success
        mockHttpResponse(MOCK_INVALID_TOKEN);   // fail

        List<String> tokens = List.of(MOCK_TOKEN, MOCK_INVALID_TOKEN);
        UserInfoCrawlRequest request = new UserInfoCrawlRequest();
        request.setTokens(tokens);

        BaseResponse<UserCrawlResult> crawlResponse = userInfoParser.batchCrawl(request);
        assertTrue(crawlResponse.isSuccess());
        assertEquals(1, crawlResponse.getResult().getUserDTOMap().size());

        assertEquals(1, crawlResponse.getResult().getFailedTokenMap().size());
        assertEquals("TokenInfos is null", crawlResponse.getResult().getFailedTokenMap().get(MOCK_INVALID_TOKEN));
    }
}