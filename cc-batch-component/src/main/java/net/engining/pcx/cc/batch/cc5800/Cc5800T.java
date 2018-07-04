package net.engining.pcx.cc.batch.cc5800;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.infrastructure.shared.model.QApGlBal;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.process.service.common.GlCalculator;
import net.engining.pg.parameter.OrganizationContextHolder;
import net.engining.pg.parameter.ParameterFacility;

public class Cc5800T implements Tasklet{
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ParameterFacility parameterFacility;
	
	private List<String> organizations;

	@Autowired
	private GlCalculator glCalculator;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		checkNotNull(organizations);
		
		for (String org : organizations)
		{
			OrganizationContextHolder.setCurrentOrganizationId(org);
			//取参数
			Map<String, Subject> subjects = parameterFacility.getParameterMap(Subject.class);
			
			//取数据
			QApGlBal q = QApGlBal.apGlBal;
			List<ApGlBal> entities = new JPAQueryFactory(em).select(q).from(q).where(q.org.eq(org)).fetch();
			
			
			
			//第一遍，建立分行/科目映射
			Map<String, ApGlBal> entityMap = Maps.newHashMap();
			for (ApGlBal bal : entities)
			{
				entityMap.put(bal.getBranchNo() + "|" + bal.getSubjectCd(), bal);
			}
			
			//第二遍，清零，鉴别非叶结点
			Set<String> nonLeaves = Sets.newHashSet();
			for (ApGlBal bal : entities)
			{
				Subject subject = subjects.get(bal.getSubjectCd());
				if (StringUtils.isNotBlank(subject.parentSubjectCd))
				{
					Subject parentSubject = subjects.get(subject.parentSubjectCd);
					nonLeaves.add(parentSubject.subjectCd);
					String parentKey = bal.getBranchNo() + "|" + parentSubject.subjectCd;
					ApGlBal parent = entityMap.get(parentKey);
					if (parent == null)
					{
						//不存在就插新的
						parent = new ApGlBal();
						parent.setOrg(org);
						parent.setBranchNo(bal.getBranchNo());
						parent.setSubjectCd(subject.parentSubjectCd);
						parent.setSubjectName(parentSubject.name);
						parent.setSubjectType(parentSubject.type);
						em.persist(parent);
						entityMap.put(parentKey, parent);
					}
					else
					{
						//存在就清零
						parent.setCrAmt(BigDecimal.ZERO);
						parent.setCrBal(BigDecimal.ZERO);
						parent.setCrCount(0);
						parent.setDbAmt(BigDecimal.ZERO);
						parent.setDbBal(BigDecimal.ZERO);
						parent.setDbCount(0);
						parent.setLastCrBal(BigDecimal.ZERO);
						parent.setLastDbBal(BigDecimal.ZERO);
						parent.setLastMthCrBal(BigDecimal.ZERO);
						parent.setLastMthDbBal(BigDecimal.ZERO);
						parent.setLastQtrCrBal(BigDecimal.ZERO);
						parent.setLastQtrDbBal(BigDecimal.ZERO);
						parent.setLastYrCrBal(BigDecimal.ZERO);
						parent.setLastYrDbBal(BigDecimal.ZERO);
					}
				}
			}
			
			//第三遍，汇总
			for (ApGlBal bal : entities)
			{
				if (nonLeaves.contains(bal.getSubjectCd()))
					continue;

				//只处理叶结点
				ApGlBal current = bal;
				while(true)
				{
					//取当前结点的父结点
					Subject subject = subjects.get(current.getSubjectCd());
					if (StringUtils.isBlank(subject.parentSubjectCd))
					{
						break;
					}
					String parentKey = current.getBranchNo() + "|" + subject.parentSubjectCd;
					ApGlBal parent = entityMap.get(parentKey);
					
					parent.setCrAmt(parent.getCrAmt().add(bal.getCrAmt()));
					parent.setCrBal(parent.getCrBal().add(bal.getCrBal()));
					parent.setCrCount(parent.getCrCount() + bal.getCrCount());
					parent.setDbAmt(parent.getDbAmt().add(bal.getDbAmt()));
					parent.setDbBal(parent.getDbBal().add(bal.getDbBal()));
					parent.setDbCount(parent.getDbCount() + bal.getDbCount());
					parent.setLastCrBal(parent.getLastCrBal().add(bal.getLastCrBal()));
					parent.setLastDbBal(parent.getLastDbBal().add(bal.getLastDbBal()));
					parent.setLastMthCrBal(parent.getLastMthCrBal().add(bal.getLastMthCrBal()));
					parent.setLastMthDbBal(parent.getLastMthDbBal().add(bal.getLastMthDbBal()));
					parent.setLastQtrCrBal(parent.getLastQtrCrBal().add(bal.getLastQtrCrBal()));
					parent.setLastQtrDbBal(parent.getLastQtrDbBal().add(bal.getLastQtrDbBal()));
					parent.setLastYrCrBal(parent.getLastYrCrBal().add(bal.getLastYrCrBal()));
					parent.setLastYrDbBal(parent.getLastYrDbBal().add(bal.getLastYrDbBal()));
					// 重算balance
					Subject parantSubject = parameterFacility.loadParameter(Subject.class, parent.getSubjectCd());
					glCalculator.reBalance(parent, parantSubject);
					
					current = parent;
				}
			}
		}
		
		return RepeatStatus.FINISHED;
	}
	
	public List<String> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<String> organizations) {
		this.organizations = organizations;
	}
}
