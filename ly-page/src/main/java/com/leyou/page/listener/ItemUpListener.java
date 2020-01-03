package com.leyou.page.listener;

import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

/**
 * 商品上架消费
 */
@Slf4j
@Component
@RocketMQMessageListener(topic=ITEM_TOPIC_NAME,
                         selectorExpression = ITEM_UP_TAGS,
                         consumerGroup = "ITEM_PAGE_UP_CONSUMER")
public class ItemUpListener implements RocketMQListener<Long> {
    @Autowired
    private PageService pageService;
    @Override
    public void onMessage(Long spuId) {
        log.info("[搜索服务]- (商品上架) -接收消息，spuId={}", spuId);
        //商品上架消息
        pageService.createHtml(spuId);
    }
}
