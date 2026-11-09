package com.quanxiaoha.xiaohashu.kv.biz.pojo.repository;

import com.quanxiaoha.xiaohashu.kv.biz.pojo.po.NoteContent;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface NoteContentRepository extends CassandraRepository<NoteContent, UUID> {
}
