package com.agent.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public class SearchOrdersRequest {

    private OffsetDateTime createdAfter;
    private OffsetDateTime createdBefore;
    private OffsetDateTime lastUpdatedAfter;
    private OffsetDateTime lastUpdatedBefore;
    private List<String> fulfillmentStatuses;
    @NotNull
    private List<String> marketplaceIds;
    private List<String> fulfilledBy;
    @Min(1)
    @Max(100)
    private Integer maxResultsPerPage;
    private String paginationToken;
    private List<String> includedData;

    @AssertTrue(message = "Exactly one of createdAfter or lastUpdatedAfter must be provided")
    public boolean isTimeFilterValid() {
        return (createdAfter != null) ^ (lastUpdatedAfter != null);
    }

    public OffsetDateTime getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(OffsetDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }

    public OffsetDateTime getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(OffsetDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }

    public OffsetDateTime getLastUpdatedAfter() {
        return lastUpdatedAfter;
    }

    public void setLastUpdatedAfter(OffsetDateTime lastUpdatedAfter) {
        this.lastUpdatedAfter = lastUpdatedAfter;
    }

    public OffsetDateTime getLastUpdatedBefore() {
        return lastUpdatedBefore;
    }

    public void setLastUpdatedBefore(OffsetDateTime lastUpdatedBefore) {
        this.lastUpdatedBefore = lastUpdatedBefore;
    }

    public List<String> getFulfillmentStatuses() {
        return fulfillmentStatuses;
    }

    public void setFulfillmentStatuses(List<String> fulfillmentStatuses) {
        this.fulfillmentStatuses = fulfillmentStatuses;
    }

    public List<String> getMarketplaceIds() {
        return marketplaceIds;
    }

    public void setMarketplaceIds(List<String> marketplaceIds) {
        this.marketplaceIds = marketplaceIds;
    }

    public List<String> getFulfilledBy() {
        return fulfilledBy;
    }

    public void setFulfilledBy(List<String> fulfilledBy) {
        this.fulfilledBy = fulfilledBy;
    }

    public Integer getMaxResultsPerPage() {
        return maxResultsPerPage;
    }

    public void setMaxResultsPerPage(Integer maxResultsPerPage) {
        this.maxResultsPerPage = maxResultsPerPage;
    }

    public String getPaginationToken() {
        return paginationToken;
    }

    public void setPaginationToken(String paginationToken) {
        this.paginationToken = paginationToken;
    }

    public List<String> getIncludedData() {
        return includedData;
    }

    public void setIncludedData(List<String> includedData) {
        this.includedData = includedData;
    }
}
