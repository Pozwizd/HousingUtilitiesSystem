package org.spacelab.housingutilitiessystemadmin.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "users")
public class UserSearchDocument {
    
    @Id
    private String id;
    
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String firstName;
    
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String middleName;
    
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String lastName;
    
    @Field(type = FieldType.Keyword)
    private String phone;
    
    @Field(type = FieldType.Keyword)
    private String email;
    
    @MultiField(mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String fullName;
    
    @Field(type = FieldType.Keyword)
    private String cityName;
    
    @Field(type = FieldType.Keyword)
    private String streetName;
    
    @Field(type = FieldType.Keyword)
    private String houseNumber;
    
    @Field(type = FieldType.Keyword)
    private String apartmentNumber;
    
    @Field(type = FieldType.Keyword)
    private String accountNumber;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Keyword)
    private String role;
}
