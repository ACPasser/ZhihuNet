package org.acpasser.zhihunet.contract.response;


import java.io.Serializable;

public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
        private boolean success;
    private String message;
    private T result;

    public BaseResponse() {
    }

    public BaseResponse(Builder<T> builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.result = builder.result;
    }

    public static Builder newSuccResponse() {
        return new Builder().success(true);
    }

    public static <T> Builder<T> success() {
        return new Builder<T>().success(true);
    }

    public static <T> Builder<T> success(T data) {
        return new Builder<T>(data).success(true);
    }

    public static Builder newFailResponse() {
        return new Builder().success(false);
    }

    public static <T> Builder<T> fail() {
        return new Builder<T>().success(false);
    }

    public static final class Builder<T> {
        private boolean success = false;
        private String message;
        private T result;

        public Builder() {
        }

        public Builder(T data) {
            this.result = data;
        }

        public BaseResponse<T> build() {
            return new BaseResponse<T>(this);
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> result(T result) {
            this.result = result;
            return this;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getmessage() {
        return message;
    }

    public T getResult() {
        return result;
    }
}