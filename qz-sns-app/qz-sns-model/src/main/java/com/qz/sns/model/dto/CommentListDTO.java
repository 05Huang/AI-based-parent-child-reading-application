package com.qz.sns.model.dto;

import com.qz.sns.model.dto.CommentDTO;
import lombok.Data;
import java.util.List;

@Data
public class CommentListDTO {
    private List<CommentDTO> comments;
    private Long total;
}