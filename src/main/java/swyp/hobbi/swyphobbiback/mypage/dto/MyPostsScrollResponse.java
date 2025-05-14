package swyp.hobbi.swyphobbiback.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyPostsScrollResponse {
    private List<MyPost> posts;
    private Boolean isLast;
}
