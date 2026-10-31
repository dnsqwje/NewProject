package kr.routineslim.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record PostDraftRequest(
        @NotNull @Size(max = 120) String title,
        @NotNull @Size(max = 40) String category,
        @NotNull @Size(max = 5000) String content,
        @NotNull @Size(max = 600) String tags,
        @Size(max = 3)
        List<@NotNull @Size(max = 4194400) String> images) {

    @AssertTrue(
            message = "Only JPG, PNG and GIF images up to 3MB are supported")
    public boolean isImagesValid() {
        return new PostRequest(
                title, category, content, null, images, null)
                .isImagesValid();
    }
}