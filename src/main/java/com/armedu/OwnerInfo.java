package com.armedu;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OwnerInfo {
        private String namespace;
        private String officialLocation;
        private String prefix;
        private String copyright;


        
    public OwnerInfo(String namespace, String officialLocation, String prefix, String copyright) {
        this.namespace = namespace;
        this.officialLocation = officialLocation;
        this.prefix = prefix;
        this.copyright = copyright;
    
    }

}
