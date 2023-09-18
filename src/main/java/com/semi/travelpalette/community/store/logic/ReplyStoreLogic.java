package com.semi.travelpalette.community.store.logic;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.semi.travelpalette.community.domain.Community;
import com.semi.travelpalette.community.domain.Reply;
import com.semi.travelpalette.community.store.ReplyStore;

@Repository
public class ReplyStoreLogic implements ReplyStore{

	@Override
	public int insertReply(SqlSession session, Reply reply) {
		int result = session.insert("ReplyMapper.insertReply", reply);
		return result;
	}

	@Override
	public List<Reply> selectReplyList(SqlSession session, Community cOne) {
		List<Reply> rList = session.selectList("ReplyMapper.selectReplyList", cOne);
		return rList;
	}

	@Override
	public int deleteReply(SqlSession session, Reply reply) {
		int result = session.update("ReplyMapper.deleteReply", reply);
		return result;
	}

}
