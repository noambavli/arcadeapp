package com.example.arcadeapp;

public interface ServerCallback<T> {
    void onSuccess(T result);
    void onError(String error);
} 