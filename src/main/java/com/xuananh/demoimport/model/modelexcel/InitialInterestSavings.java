package com.xuananh.demoimport.model.modelexcel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitialInterestSavings {
    private String baoLocAccount;
    private String regularAccount;
}
