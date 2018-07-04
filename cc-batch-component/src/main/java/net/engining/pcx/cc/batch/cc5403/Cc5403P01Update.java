package net.engining.pcx.cc.batch.cc5403;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.ApInternalSubjectSum;
import net.engining.pcx.cc.infrastructure.shared.model.QApGlBal;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.process.service.common.GlCalculator;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 各级行会计登帐
 * <p>
 * 将会计分录登帐到各级支行
 * <p>
 * 输入接口：当日会计分录临时表 输出接口：分支行登帐失败报表接口
 * 
 * @author heyu.wang
 * 
 */
public class Cc5403P01Update implements ItemProcessor<ApInternalSubjectSum, ApInternalSubjectSum> {

	@Autowired
	private ParameterFacility parameterCacheFacility;
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private GlCalculator glCalculator;

	@Override
	public ApInternalSubjectSum process(ApInternalSubjectSum item) throws Exception {
		
		QApGlBal q = QApGlBal.apGlBal ;
		ApGlBal agb = new JPAQueryFactory(em)
				.select(q)
				.from(q)
				.where(q.branchNo.eq(item.getOwingBranch()), q.subjectCd.eq(item.getSubjectCd()))
				.fetchOne();
		
		if (agb == null)
			throw new RuntimeException("科目初始化错误, " + "branchNo:" + item.getOwingBranch() +  ", titleCd:" + item.getSubjectCd());

		agb.setCrAmt(agb.getCrAmt().add(item.getCrAmt()));
		agb.setDbAmt(agb.getDbAmt().add(item.getDbAmt()));
		agb.setCrBal(agb.getCrBal().add(item.getCrBal()));
		agb.setDbBal(agb.getDbBal().add(item.getDbBal()));
		agb.setDbCount(agb.getDbCount() + item.getDbCount());
		agb.setCrCount(agb.getCrCount() + item.getCrCount());
		
		// 重算balance
		Subject subject = parameterCacheFacility.loadParameter(Subject.class, item.getSubjectCd());
		glCalculator.reBalance(agb, subject);
		return item;
	}

}
