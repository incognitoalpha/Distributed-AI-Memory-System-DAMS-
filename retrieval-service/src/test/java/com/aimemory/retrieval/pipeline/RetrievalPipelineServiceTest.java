package com.aimemory.retrieval.pipeline;

import com.aimemory.shared.exception.RetrievalPipelineException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrievalPipelineServiceTest {

    @Mock
    private QueryRewriterService queryRewriter;

    @Mock
    private HybridSearchService hybridSearch;

    @Mock
    private ReRankingService reRanker;

    @Mock
    private ContextBudgetAllocator budgetAllocator;

    private RetrievalPipelineService pipelineService;

    @BeforeEach
    void setUp() {
        pipelineService = new RetrievalPipelineService(
                queryRewriter,
                hybridSearch,
                reRanker,
                budgetAllocator,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void retrieve_wrapsUnexpectedFailuresInTypedException() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(queryRewriter.generateHypotheticalDocument("my query"))
                .thenReturn("hyde");
        when(hybridSearch.search("hyde", "my query", tenantId, userId, 50))
                .thenThrow(new IllegalStateException("boom"));

        RetrievalPipelineException ex = assertThrows(
                RetrievalPipelineException.class,
                () -> pipelineService.retrieve("my query", tenantId, userId, 2048)
        );

        assertEquals("RETRIEVAL_PIPELINE_ERROR", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Pipeline failed"));
        assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
        verify(queryRewriter).generateHypotheticalDocument("my query");
        verify(hybridSearch).search("hyde", "my query", tenantId, userId, 50);
        verifyNoInteractions(reRanker, budgetAllocator);
    }

    @Test
    void retrieve_returnsEmptyListAndRecordsMissWhenNoCandidates() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(queryRewriter.generateHypotheticalDocument("my query")).thenReturn("hyde");
        when(hybridSearch.search("hyde", "my query", tenantId, userId, 50)).thenReturn(List.of());

        List<ReRankingService.RankedMemory> result = pipelineService.retrieve("my query", tenantId, userId, 2048);

        assertTrue(result.isEmpty());
        verifyNoInteractions(reRanker, budgetAllocator);
    }
}
