// Shapes returned by the Spring Boot API.

export type EmploymentType = "FULL_TIME" | "PART_TIME" | "CONTRACT" | "INTERNSHIP";
export type JobStatus = "OPEN" | "CLOSED";
export type ApplicationStatus = "PENDING" | "ACCEPTED" | "REJECTED";

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface JobSummary {
  id: number;
  title: string;
  location: string;
  employmentType: EmploymentType;
  closingDate: string;
  summary: string;
}

export interface JobDetail extends JobSummary {
  description: string;
  responsibilities: string;
  requirements: string;
  status: JobStatus;
  acceptingApplications: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Counts {
  total: number;
  pending: number;
  accepted: number;
  rejected: number;
}

export interface AdminJobRow {
  id: number;
  title: string;
  location: string;
  employmentType: EmploymentType;
  closingDate: string;
  status: JobStatus;
  acceptingApplications: boolean;
  applications: Counts;
}

export interface Overview {
  totals: Counts;
  jobs: AdminJobRow[];
}

export interface ApplicationSummary {
  id: number;
  jobId: number;
  fullName: string;
  email: string;
  phone: string;
  status: ApplicationStatus;
  createdAt: string;
}

export interface ApplicationDetail {
  id: number;
  jobId: number;
  jobTitle: string;
  fullName: string;
  email: string;
  phone: string;
  coverNote: string;
  cvFileName: string;
  cvSizeBytes: number;
  status: ApplicationStatus;
  decisionNote: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApiErrorBody {
  status: number;
  message: string;
  errors?: Record<string, string>;
}
