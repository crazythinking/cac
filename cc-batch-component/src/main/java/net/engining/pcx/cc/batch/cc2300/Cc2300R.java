package net.engining.pcx.cc.batch.cc2300;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.querydsl.jpa.impl.JPAQueryFactory;

import net.engining.pcx.cc.infrastructure.shared.enums.LoanStatus;
import net.engining.pcx.cc.infrastructure.shared.model.CactLoan;
import net.engining.pcx.cc.infrastructure.shared.model.QCactLoan;
import net.engining.pg.batch.sdk.AbstractKeyBasedReader;

@Service
@Scope("step")
public class Cc2300R extends AbstractKeyBasedReader<Integer, CactLoan> {
	@PersistenceContext
	protected EntityManager em;

	@Override
	protected List<Integer> loadKeys() {
		QCactLoan qCactLoan = QCactLoan.cactLoan;
		return new JPAQueryFactory(em)
				.select(qCactLoan.loanId)
				.from(qCactLoan).where(qCactLoan.loanStatus.eq(LoanStatus.I)).fetch();
	}

	@Override
	protected CactLoan loadItemByKey(Integer key) {
		return em.find(CactLoan.class, key);
	}
}
