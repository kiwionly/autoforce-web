package com.github.autoforce.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;;

@Target({ TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller
{
    String name();

    String description();

    String url();
}