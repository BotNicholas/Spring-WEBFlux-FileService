package org.example.videoviewer.services.models;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class VideoResponse {
    private StreamingResponseBody responseBody;
    private Long start;
    private Long end;
    private Long totalBytes;

    public VideoResponse() {
    }

    public VideoResponse(StreamingResponseBody responseBody, Long start, Long end, Long totalBytes) {
        this.responseBody = responseBody;
        this.start = start;
        this.end = end;
        this.totalBytes = totalBytes;
    }

    public StreamingResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(StreamingResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }
}
