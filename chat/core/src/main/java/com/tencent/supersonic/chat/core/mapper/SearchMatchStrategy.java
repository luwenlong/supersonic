package com.tencent.supersonic.chat.core.mapper;

import com.google.common.collect.Lists;
import com.tencent.supersonic.headless.api.pojo.response.S2Term;
import com.tencent.supersonic.headless.core.knowledge.HanlpMapResult;
import com.tencent.supersonic.headless.core.knowledge.SearchService;
import com.tencent.supersonic.chat.core.pojo.QueryContext;
import com.tencent.supersonic.common.pojo.enums.DictWordType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * SearchMatchStrategy encapsulates a concrete matching algorithm
 * executed during search process.
 */
@Service
public class SearchMatchStrategy extends BaseMatchStrategy<HanlpMapResult> {

    private static final int SEARCH_SIZE = 3;

    @Override
    public Map<MatchText, List<HanlpMapResult>> match(QueryContext queryContext, List<S2Term> originals,
            Set<Long> detectViewIds) {
        String text = queryContext.getQueryText();
        Map<Integer, Integer> regOffsetToLength = getRegOffsetToLength(originals);

        List<Integer> detectIndexList = Lists.newArrayList();

        for (Integer index = 0; index < text.length(); ) {

            if (index < text.length()) {
                detectIndexList.add(index);
            }
            Integer regLength = regOffsetToLength.get(index);
            if (Objects.nonNull(regLength)) {
                index = index + regLength;
            } else {
                index++;
            }
        }
        Map<MatchText, List<HanlpMapResult>> regTextMap = new ConcurrentHashMap<>();
        detectIndexList.stream().parallel().forEach(detectIndex -> {
                    String regText = text.substring(0, detectIndex);
                    String detectSegment = text.substring(detectIndex);

                    if (StringUtils.isNotEmpty(detectSegment)) {
                        List<HanlpMapResult> hanlpMapResults = SearchService.prefixSearch(detectSegment,
                                SearchService.SEARCH_SIZE, detectViewIds);
                        List<HanlpMapResult> suffixHanlpMapResults = SearchService.suffixSearch(
                                detectSegment, SEARCH_SIZE, detectViewIds);
                        hanlpMapResults.addAll(suffixHanlpMapResults);
                        // remove entity name where search
                        hanlpMapResults = hanlpMapResults.stream().filter(entry -> {
                            List<String> natures = entry.getNatures().stream()
                                    .filter(nature -> !nature.endsWith(DictWordType.ENTITY.getType()))
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(natures)) {
                                return false;
                            }
                            return true;
                        }).collect(Collectors.toList());
                        MatchText matchText = MatchText.builder()
                                .regText(regText)
                                .detectSegment(detectSegment)
                                .build();
                        regTextMap.put(matchText, hanlpMapResults);
                    }
                }
        );
        return regTextMap;
    }

    @Override
    public boolean needDelete(HanlpMapResult oneRoundResult, HanlpMapResult existResult) {
        return false;
    }

    @Override
    public String getMapKey(HanlpMapResult a) {
        return null;
    }

    @Override
    public void detectByStep(QueryContext queryContext, Set<HanlpMapResult> results, Set<Long> detectViewIds,
            Integer startIndex,
            Integer i, int offset) {

    }
}
