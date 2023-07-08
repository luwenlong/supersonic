package com.tencent.supersonic.semantic.core.domain.manager;

import com.tencent.supersonic.semantic.api.core.pojo.yaml.MetricYamlTpl;
import com.tencent.supersonic.semantic.core.domain.pojo.Metric;
import com.tencent.supersonic.semantic.core.domain.utils.MetricConverter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MetricYamlManager {


    public static List<MetricYamlTpl> convert2YamlObj(List<Metric> metrics) {

        List<MetricYamlTpl> metricYamlTpls = new ArrayList<>();
        for (Metric metric : metrics) {
            MetricYamlTpl metricYamlTpl = MetricConverter.convert2MetricYamlTpl(metric);
            metricYamlTpls.add(metricYamlTpl);
        }
        return metricYamlTpls;
    }

}
