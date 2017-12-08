package cn.com.sequence.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

@Service
public class SequenceService {
	@Autowired
	LockSquence lockSquence;

	int expand = 200000; //id段位
	Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Long getSequenceId(String ip) {
		synchronized (lockSquence) {
			long id = lockSquence.getAtomic().incrementAndGet();
			Long maxId = lockSquence.getCurrentMaxId().get(ip) ;
			if (maxId != null && id < maxId) {
				log.info("生成的id：{}", id);
				return id;
			}
		}
		
		Long getId = updateSequenceId(ip);
		Long currentId = getId * expand;
		synchronized (lockSquence) {
			lockSquence.getAtomic().set(currentId);
			lockSquence.getCurrentMaxId().put(ip, (getId + 1) * expand);
		}
		log.info("生成的id：{}", currentId);
		return currentId;
	}

//	AtomicLong atomic = new AtomicLong();
//	private static ConcurrentMap<String, Long> currentMaxId = new ConcurrentHashMap<>();
//	public Long getSequenceId(String ip) {
//		long id = atomic.incrementAndGet();
//		Long maxId = currentMaxId.get(ip);
//		synchronized (maxId) {
//			if (maxId != null && id < maxId) {
//				log.info("生成的id：{}", id);
//				return id;
//			}
//		}
//		
//		Long getId = updateSequenceId(ip);
//		Long currentId = getId * expand;
//		atomic.set(currentId);
//		currentMaxId.put(ip, (getId + 1) * expand);
//		log.info("生成的id：{}", id);
//		return currentId;
//	}

	public long updateSequenceId(final String ip) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				final PreparedStatement ps = connection.prepareStatement(
						"INSERT INTO sequence_generator_table (ip) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, ip);
				return ps;
			}
		}, keyHolder);
		Long id = keyHolder.getKey().longValue();
		jdbcTemplate.update("delete from sequence_generator_table where ip=? and id<? " , new Object[] { ip,id },new int[] { Types.VARCHAR,  Types.BIGINT });
		return id;
	}
}
