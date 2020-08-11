package com.groocraft.couchdb.slacker.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo class to ease reading responses to put requests.
 *
 * @author Majlanky
 */
@NoArgsConstructor
@Getter
@Setter
public class DocumentPutResponse {

    private String ok;
    private String id;
    private String rev;
    private String error;

}
