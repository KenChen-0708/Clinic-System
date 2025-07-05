/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package adt;

public interface ListInterface<T> {
    public void add(T data);
    public boolean remove(T data);
    public T get(int index);
    public int size();
    public boolean isEmpty();
    public void clear();
    public boolean contains(T data);
}
