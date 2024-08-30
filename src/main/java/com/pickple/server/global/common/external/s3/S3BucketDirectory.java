package com.pickple.server.global.common.external.s3;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum S3BucketDirectory {

    MOIM_PREFIX("moim/"),
    NOTICE_PREFIX("notice/"),
    REVIEW_PREFIX("review/"),
    HOST_PREFIX("host/");

    private final String name;

    public String value() {
        return this.name;
    }
}
