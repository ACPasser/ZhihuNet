package org.acpasser.zhihunet.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.contract.dto.activity.AnswerDTO;
import org.acpasser.zhihunet.contract.dto.activity.ArticleDTO;
import org.acpasser.zhihunet.contract.dto.activity.CollectionDTO;
import org.acpasser.zhihunet.contract.dto.activity.PinDTO;
import org.acpasser.zhihunet.contract.dto.activity.QuestionDTO;
import org.acpasser.zhihunet.contract.dto.activity.TopicDTO;
import org.acpasser.zhihunet.contract.dto.activity.InteractionDTO;
import org.acpasser.zhihunet.crawler.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserActivityParserDTO {
    // => ZhihuUserInteraction
    private Long id;
    private String type;            // 知乎字段，暂不清楚
    private String verb;            // 知乎字段，暂不清楚
    private Integer createdTime;       // => interaction_time
    private String actionText;      // 互动类型文本
    private BaseEntity.UserEntity actor;
    private BaseEntity target;

    public InteractionDTO getInteractionDTO() {
        InteractionDTO interactionDTO = new InteractionDTO();
        BeanUtils.copyProperties(this, interactionDTO);
        interactionDTO.setInteractionId(this.id);
        interactionDTO.setActorToken(this.actor.getUrlToken());
        if (this.target.isCollection()) {
            // "收藏夹"使用的是Creator
            CollectionEntity collectionEntity = (CollectionEntity) this.target;
            interactionDTO.setAuthorToken(collectionEntity.getCreator().getUrlToken());
            interactionDTO.setIsFollowing(collectionEntity.getCreator().getIsFollowing());
            interactionDTO.setIsFollowed(collectionEntity.getCreator().getIsFollowed());
        } else {
            interactionDTO.setAuthorToken(this.target.getAuthor().getUrlToken());
            interactionDTO.setIsFollowing(this.target.getAuthor().getIsFollowing());
            interactionDTO.setIsFollowed(this.target.getAuthor().getIsFollowed());
        }


        interactionDTO.setTargetId(this.target.getId());
        interactionDTO.setTargetType(this.getTarget().getType().getType());
        interactionDTO.setInteractionTime(new Date(this.getCreatedTime() * 1000L));
        return interactionDTO;
    }

    public AnswerDTO getAnswerDTO() {
        if (this.target == null || !this.target.isAnswer()) {
            return null;
        }
        AnswerEntity answerEntity = (AnswerEntity) this.target;
        AnswerDTO answerDTO = new AnswerDTO();
        BeanUtils.copyProperties(answerEntity, answerDTO);
        answerDTO.setAnswerId(answerEntity.getId());
        answerDTO.setAuthorToken(answerEntity.getAuthor().getUrlToken());
        answerDTO.setAnswerCreatedTime(new Date(answerEntity.getCreatedTime() * 1000L));
        answerDTO.setAnswerUpdatedTime(new Date(answerEntity.getUpdatedTime() * 1000L));
        answerDTO.setExcerpt(answerEntity.getExcerptNew());
        answerDTO.setCanComment(JsonUtils.toJson(answerEntity.getCanComment()));
        answerDTO.setQuestionId(answerEntity.getQuestion().getId());
        return answerDTO;
    }

    public QuestionDTO getQuestionDTO() {
        if (this.target == null) {
            return null;
        }
        QuestionEntity questionEntity;
        if (this.target.isQuestion()) {
            questionEntity = (QuestionEntity) this.target;
        } else if (this.target.isAnswer()) {
            AnswerEntity answerEntity = (AnswerEntity) this.target;
            questionEntity = answerEntity.getQuestion();
        } else {
            return null;
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(questionEntity, questionDTO);
        questionDTO.setQuestionId(questionEntity.getId());
        questionDTO.setAuthorToken(questionEntity.getAuthor().getUrlToken());
        questionDTO.setQuestionCreatedTime(new Date(questionEntity.getCreated() * 1000L));
        questionDTO.setBoundTopicIds(StringUtils.join(questionEntity.getBoundTopicIds(), ","));
        return questionDTO;
    }

    public ArticleDTO getArticleDTO() {
        if (this.target == null || !this.target.isArticle()) {
            return null;
        }
        ArticleEntity articleEntity = (ArticleEntity) this.target;
        ArticleDTO articleDTO = new ArticleDTO();
        BeanUtils.copyProperties(articleEntity, articleDTO);
        articleDTO.setArticleId(articleEntity.getId());
        articleDTO.setAuthorToken(articleEntity.getAuthor().getUrlToken());
        articleDTO.setArticleCreatedTime(new Date(articleEntity.getCreated() * 1000L));
        articleDTO.setArticleUpdatedTime(new Date(articleEntity.getUpdated() * 1000L));
        return articleDTO;
    }

    public PinDTO getPinDTO() {
        if (this.target == null || !this.target.isPin()) {
            return null;
        }
        PinEntity pinEntity = (PinEntity) this.target;
        PinDTO pinDTO = new PinDTO();
        BeanUtils.copyProperties(pinEntity, pinDTO);
        pinDTO.setPinId(pinEntity.getId());
        pinDTO.setAuthorToken(pinEntity.getAuthor().getUrlToken());
        pinDTO.setPinCreatedTime(new Date(pinEntity.getCreated() * 1000L));
        pinDTO.setAdminCloseRepin(pinEntity.getIsAdminCloseRepin());
        pinDTO.setContainAiContent(pinEntity.getIsContainAiContent());

        List<Long> topicIds = Optional.ofNullable(pinEntity.getTopics())
                .orElse(Collections.emptyList())
                .stream()
                .map(PinEntity.TopicEntity::getId)
                .toList();
        pinDTO.setTopicIds(StringUtils.join(topicIds, ","));
        return pinDTO;
    }

    public List<TopicDTO> getTopicDTOS() {
        if (this.target == null) {
            return null;
        }
        if (this.target.isPin()) {
            PinEntity pinEntity = (PinEntity) this.getTarget();
            List<PinEntity.TopicEntity> topics = pinEntity.getTopics();
            if (CollectionUtils.isEmpty(topics)) {
                return Collections.emptyList();
            }
            List<TopicDTO> topicDTOS = new ArrayList<>();
            for (PinEntity.TopicEntity topic : topics) {
                TopicDTO topicDTO = new TopicDTO();
                BeanUtils.copyProperties(topic, topicDTO);
                // 这里将原始数据的id换成topic_id
                topicDTO.setTopicId(topic.getId());
                topicDTO.setId(null);
                topicDTOS.add(topicDTO);
            }
            return topicDTOS;
        } else {
            return null;
        }
    }

    public CollectionDTO getCollectionDTO() {
        if (this.target == null || !this.target.isCollection()) {
            return null;
        }
        CollectionEntity collectionEntity = (CollectionEntity) this.target;
        CollectionDTO collectionDTO = new CollectionDTO();
        BeanUtils.copyProperties(collectionEntity, collectionDTO);
        collectionDTO.setCollectionId(collectionEntity.getId());
        collectionDTO.setCreatorToken(collectionEntity.getCreator().getUrlToken()); // 注：这里不是author
        collectionDTO.setCollectionCreatedTime(new Date(collectionEntity.getCreatedTime() * 1000L));
        collectionDTO.setCollectionUpdatedTime(new Date(collectionEntity.getUpdatedTime() * 1000L));
        collectionDTO.setPublicStatus(collectionEntity.getIsPublic());
        collectionDTO.setPublicStatus(collectionEntity.getIsPublic());

        return collectionDTO;
    }
}
