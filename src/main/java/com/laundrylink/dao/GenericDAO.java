package com.laundrylink.dao;

import java.util.List;

public interface GenericDAO<T> {
    boolean insert(T item);
    T findById(int id);
    List<T> findAll();
    boolean update(T item);
    boolean delete(int id);
}