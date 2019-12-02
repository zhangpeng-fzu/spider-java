package com.peng.service.impl;

import com.peng.mapper.BaseMapper;
import com.peng.service.BaseService;

public abstract class BaseServiceImpl<T> implements BaseService<T> {
    protected abstract BaseMapper<T> getMapper();

    @Override
    public int insert(T record) {
        return getMapper().insert(record);
    }


}