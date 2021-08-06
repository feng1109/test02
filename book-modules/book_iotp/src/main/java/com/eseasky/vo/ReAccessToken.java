package com.eseasky.vo;

import lombok.Data;

@Data
public class ReAccessToken {
    private String accessToken;
    private Integer expiresIn;
}
