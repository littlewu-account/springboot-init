package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 吴峻阳
 * @version 1.0
 */

@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if(StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }

        //可以设置不公平分发（限流）
        //channel.basicQos(2);

        //这里的chart信息要从数据库中获取
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR, "图表信息为空");
        }
        //异步调用AI开始时，将chart信息更改为：status: running
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean updateRunning = chartService.updateById(updateChart);
        if(!updateRunning) {
            //这里调用公用的方法，将图表信息更新为失败状态
            channel.basicNack(deliveryTag, false, false);
            handChartUpdateError(chart.getId(), "更新图表为执行中状态失败");
            return;
        }
        String finalResult = aiManager.doChat(CommonConstant.BI_MODEL_ID, buildUserInput(chart));
        //数据拆分
        String[] split = finalResult.split("【【【【【");
        if (split.length < 3) {
            handChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        //异步调用AI结束时，将chant信息更改为：status:success
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setStatus("success");
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        boolean updateSuccess = chartService.updateById(updateChartResult);
        if(!updateSuccess) {
            handChartUpdateError(chart.getId(), "更新图表为成功状态失败");
            return;
        }
        //消息确认
        channel.basicAck(deliveryTag, false);
    }

    private String buildUserInput(Chart chart) {
        String userGoal = chart.getGoal();
        String chartType = chart.getChartType();
        String result = chart.getChartData();
        StringBuilder userInput = new StringBuilder();
        //用户数据拼接（按照格式）
        userInput.append("分析需求").append("\n");
        if (StringUtils.isNoneBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(result);
        return userInput.toString();
    }

    public void handChartUpdateError(Long id, String exeMessage) {
        Chart chart = new Chart();
        chart.setId(id);
        chart.setStatus("fail");
        chart.setExecMessage(exeMessage);
        boolean b = chartService.updateById(chart);
        if(!b) {
            log.error("更新图表:" + id + "失败状态失败");
        }
    }

}
