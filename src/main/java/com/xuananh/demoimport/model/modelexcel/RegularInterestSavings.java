package com.xuananh.demoimport.model.modelexcel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegularInterestSavings {
    private String monthly;
    private String quarterly;
}
