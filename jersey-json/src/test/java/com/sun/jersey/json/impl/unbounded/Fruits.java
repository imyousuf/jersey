//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.01 at 06:20:44 PM CEST 
//


package com.sun.jersey.json.impl.unbounded;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fruit" type="{}Fruit" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fruit"
})
@XmlRootElement(name = "fruits")
public class Fruits {

    protected Fruit fruit;

    /**
     * Gets the value of the fruit property.
     * 
     * @return
     *     possible object is
     *     {@link Fruit }
     *     
     */
    public Fruit getFruit() {
        return fruit;
    }

    /**
     * Sets the value of the fruit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fruit }
     *     
     */
    public void setFruit(Fruit value) {
        this.fruit = value;
    }

}
