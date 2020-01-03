package com.leyou.sms.listener;

import com.leyou.sms.utils.SmsHelper;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.common.constants.RocketMQConstants.CONSUMER.SMS_VERIFY_CODE_CONSUMER;
import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.sms.constants.SmsConstants.*;

@Component
@RocketMQMessageListener(consumerGroup = SMS_VERIFY_CODE_CONSUMER,
        topic = SMS_TOPIC_NAME,
        selectorExpression = VERIFY_CODE_TAGS,
        messageModel = MessageModel.CLUSTERING)
public class SmsListener implements RocketMQListener<Map> {
    @Autowired
    private SmsHelper smsHelper;

    @Override
    public void onMessage(Map map) {
        String phoneNumbers = map.get(SMS_PARAM_KEY_PHONE).toString();
        String signName = map.get(SMS_PARAM_KEY_SIGN_NAME).toString();
        String templateCode = map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();
        String templateParam = map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();
        smsHelper.sendMessage(phoneNumbers,signName,templateCode,templateParam);
    }
}
