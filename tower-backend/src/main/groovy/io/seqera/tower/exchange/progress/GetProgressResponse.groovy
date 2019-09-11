package io.seqera.tower.exchange.progress

import io.seqera.tower.exchange.BaseResponse

class GetProgressResponse implements BaseResponse {

    ProgressData progress
    String message
}
