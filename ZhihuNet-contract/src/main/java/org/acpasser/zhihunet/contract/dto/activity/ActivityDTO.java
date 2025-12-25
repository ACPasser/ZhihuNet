package org.acpasser.zhihunet.contract.dto.activity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO implements Serializable {

    @Builder.Default
    private List<InteractionDTO> interactionDTOS = new ArrayList<>();
    @Builder.Default
    private List<QuestionDTO> questionDTOS = new ArrayList<>();
    @Builder.Default
    private List<AnswerDTO> answerDTOS = new ArrayList<>();
    @Builder.Default
    private List<ArticleDTO> articleDTOS = new ArrayList<>();
    @Builder.Default
    private List<PinDTO> pinDTOS = new ArrayList<>();
    @Builder.Default
    private List<TopicDTO> topicDTOS = new ArrayList<>();
    @Builder.Default
    private List<CollectionDTO> collectionDTOS = new ArrayList<>();

    public void merge(ActivityDTO activityDTO) {
        this.interactionDTOS.addAll(activityDTO.interactionDTOS);
        // 收集关联的问题、回答、文章、想法、话题
        this.questionDTOS.addAll(activityDTO.questionDTOS);
        this.answerDTOS.addAll(activityDTO.answerDTOS);
        this.articleDTOS.addAll(activityDTO.articleDTOS);
        this.pinDTOS.addAll(activityDTO.pinDTOS);
        this.topicDTOS.addAll(activityDTO.topicDTOS);
        this.collectionDTOS.addAll(activityDTO.collectionDTOS);
    }

}
