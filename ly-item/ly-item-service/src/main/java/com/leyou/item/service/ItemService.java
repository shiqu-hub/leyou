package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.pojo.Item;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    public Item save(Item item){
        if (item.getPrice()==null){
            throw new LyException(ExceptionEnum.ITEM_PRICE_NOT_NULL);
        }
        item.setId(100);
        return item;
    }
}
