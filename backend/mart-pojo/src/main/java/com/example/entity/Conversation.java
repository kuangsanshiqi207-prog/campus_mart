package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userAId;

    private Long userBId;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Integer aUnread;

    private Integer bUnread;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}