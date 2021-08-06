package com.eseasky.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatisPlus自动填充配置
 */
@Slf4j
@Component
public class SurveyMetaObjectHandle implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("自动插入表创建时间");
        /*this.setFieldValByName("createTime",new Date(),metaObject );
        this.setFieldValByName("updateTime",new Date(),metaObject );*/

        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("自动插入表更新时间");
       // this.setFieldValByName("updateTime",new Date(),metaObject );
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐)
    }


}
