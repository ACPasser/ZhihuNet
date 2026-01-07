import com.fasterxml.jackson.core.type.TypeReference;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.acpasser.zhihunet.contract.dto.activity.ActivityCrawlResult;
import org.acpasser.zhihunet.contract.dto.activity.ActivityDTO;
import org.acpasser.zhihunet.contract.request.ActivityCrawlRequest;
import org.acpasser.zhihunet.contract.response.BaseResponse;
import org.acpasser.zhihunet.crawler.config.CrawlerConfigProperties;
import org.acpasser.zhihunet.crawler.dto.ZhihuUserActivityParserDTO;
import org.acpasser.zhihunet.crawler.parser.ActivityParser;
import org.acpasser.zhihunet.crawler.util.HttpClientUtil;
import org.acpasser.zhihunet.crawler.util.JsonUtils;
import org.acpasser.zhihunet.crawler.util.ParseUtil;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuAnswerRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuArticleRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuCollectionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuPinRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuQuestionRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuTopicRepository;
import org.acpasser.zhihunet.repository.mybatis.repository.ZhihuUserInteractionRepository;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivityParserTest {

    @InjectMocks
    private ActivityParser activityParser;

    @Mock
    private HttpClientUtil httpClientUtil;
    @Mock
    private CrawlerConfigProperties config;
    @Mock
    private ZhihuUserInteractionRepository interactionRepository;
    @Mock
    private ZhihuQuestionRepository questionRepository;
    @Mock
    private ZhihuAnswerRepository answerRepository;
    @Mock
    private ZhihuArticleRepository articleRepository;
    @Mock
    private ZhihuPinRepository pinRepository;
    @Mock
    private ZhihuTopicRepository topicRepository;
    @Mock
    private ZhihuCollectionRepository collectionRepository;

    private MockedStatic<ParseUtil> mockedParseUtil;

    private static final int MOCK_BATCH_SIZE = 1;
    private static final int MOCK_THREAD_COUNT = 1;
    private static final String MOCK_XZSE93 = "mock_xzse93";
    private static final String MOCK_DC0 = "mock_dc0";
    private static final String MOCK_ZSE96 = "mock_zse96";

    private static final String TEST_ACTIVITY_JSON = "activity_example.json";
    private static final String TEST_COLLECTION_JSON = "activity_collection.json";
    private static final String TEST_PIN_JSON = "activity_pin.json";
    private static final String TEST_QUESTION_JSON = "activity_question.json";
    private static final String TEST_ANSWER_JSON = "activity_answer.json";
    private static final String TEST_ARTICLE_JSON = "activity_article.json";
    private static final String TEST_INTEGRATION_JSON = "activity_integration.json";

    private String readFile(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new BusinessException("测试文件不存在: " + path);
        }
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining());
    }

    @BeforeEach
    public void setUp() {
        // ========== Mock 配置参数（避免初始化 NPE） ==========
        when(config.getBatchSize()).thenReturn(MOCK_BATCH_SIZE);
        when(config.getThreadCount()).thenReturn(MOCK_THREAD_COUNT);
        lenient().when(config.getXZse93()).thenReturn(MOCK_XZSE93);
        lenient().when(config.getDC0()).thenReturn(MOCK_DC0);

        activityParser.init();

        // ========== Mock 静态工具类 ==========
        mockedParseUtil = mockStatic(ParseUtil.class);
        mockedParseUtil.when(() -> ParseUtil.parseZse96(anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(MOCK_ZSE96);

        // ========== Mock HTTP 请求 ==========
        Document mockDoc = mock(Document.class, RETURNS_DEEP_STUBS);    // 支持链式调用
        lenient().when(mockDoc.body().text()).thenReturn(readFile(TEST_ACTIVITY_JSON));
        lenient().when(httpClientUtil.doGetWithRetry(any())).thenReturn(mockDoc);
    }

    @AfterEach
    public void tearDown() {
        if (mockedParseUtil != null) {
            mockedParseUtil.close();
        }
    }

    @Test
    public void testActivityDeserialize() {
        //【类型1】知乎回答
        String answerActJson = readFile(TEST_ANSWER_JSON);
        ZhihuUserActivityParserDTO ansActParserDTO = JsonUtils.fromJson(answerActJson, ZhihuUserActivityParserDTO.class);
        assertNotNull(ansActParserDTO, "解析结果为null");

        //【类型2】知乎问题
        String questionActJson = readFile(TEST_QUESTION_JSON);
        ZhihuUserActivityParserDTO queActParserDTO = JsonUtils.fromJson(questionActJson, ZhihuUserActivityParserDTO.class);
        assertNotNull(queActParserDTO, "解析结果为null");

        //【类型3】知乎文章
        String articleJson = readFile(TEST_ARTICLE_JSON);
        ZhihuUserActivityParserDTO artActParserDTO = JsonUtils.fromJson(articleJson, new TypeReference<>() {
        });
        assertNotNull(artActParserDTO, "解析结果为null");

        //【类型4】知乎想法
        String pinJson = readFile(TEST_PIN_JSON);
        ZhihuUserActivityParserDTO pinActParserDTO = JsonUtils.fromJson(pinJson, new TypeReference<>() {
        });
        assertNotNull(pinActParserDTO, "解析结果为null");

        //【类型5】知乎收藏
        String collectionJson = readFile(TEST_COLLECTION_JSON);
        ZhihuUserActivityParserDTO colActParserDTO = JsonUtils.fromJson(collectionJson, new TypeReference<>() {});
        assertNotNull(colActParserDTO, "解析结果为null");

        // 混合5种类型
        String actionJson = readFile(TEST_INTEGRATION_JSON);
        List<ZhihuUserActivityParserDTO> activityParserDTOList = JsonUtils.fromJson(actionJson, new TypeReference<>() {
        });
        assertNotNull(activityParserDTOList);
        assertEquals(5, activityParserDTOList.size());
    }

    @Test
    public void testBatchCrawlRecentActivityByLimit() {
        String testToken = "testToken";
        List<String> tokens = new ArrayList<>();
        tokens.add(testToken);
        ActivityCrawlRequest request = ActivityCrawlRequest.builder()
            .tokens(tokens)
            .limit(1)
            .build();
        when(interactionRepository.listAll()).thenReturn(new ArrayList<>());
        BaseResponse<ActivityCrawlResult> response = activityParser.batchCrawl(request);
        assertTrue(response.isSuccess());

        Map<String, ActivityDTO> activityDTOMap = response.getResult().getActivityDTOMap();
        assertEquals(tokens.size(), activityDTOMap.size());
        assertFalse(activityDTOMap.get(testToken).getInteractionDTOS().isEmpty());
    }

    @Test
    public void testBatchCrawlRecentActivityByDuration() {
        String testToken = "testToken";
        List<String> tokens = new ArrayList<>();
        tokens.add(testToken);
        ActivityCrawlRequest request = ActivityCrawlRequest.builder()
            .tokens(tokens)
            .duration(365)
            .timeUnit(TimeUnit.DAYS)
            .build();
        BaseResponse<ActivityCrawlResult> response = activityParser.batchCrawl(request);
        assertTrue(response.isSuccess());
        Map<String, ActivityDTO> activityDTOMap = response.getResult().getActivityDTOMap();
        assertEquals(tokens.size(), activityDTOMap.size());
    }
}