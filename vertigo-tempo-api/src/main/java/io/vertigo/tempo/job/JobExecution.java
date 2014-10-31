package io.vertigo.tempo.job;

import io.vertigo.lang.Option;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import java.util.Date;
import java.util.UUID;

public final class JobExecution {
	public static enum JobStatus {
		Pending,
		Running,
		Failed,
		Succeeded
	}

	private JobDefinition jobDefinition;
	private UUID uid;
	private Date startPendingDate;
	private Option<Date> startRunningDate;
	private Option<Date> endDate;

	public JobDefinition getJobDefinition() {
		return jobDefinition;
	}

	public Date getPendingDate() {
		return startPendingDate;
	}

	public Option<Long> getRunningDurationMs() {
		return 0;
	}
	//
	//	public Option<Date> getEndDate() {
	//		return endDate;
	//	}
}
