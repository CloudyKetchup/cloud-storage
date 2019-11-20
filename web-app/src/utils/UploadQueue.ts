import { APIHelpers as API } from '../helpers';

import { UploadItem } from '../model/UploadItem';

import axios from "axios";

export class UploadJob {
	private cancelToken = axios.CancelToken;
	private cancelTokenSource = this.cancelToken.source();
	data : UploadItem;
	canceled = false;

	constructor(data : UploadItem) {
		this.data = data;
	};

	start = async () : Promise<UploadJob> => {
		await API.uploadFile(this.data.file, this.data.folder, this.data, this.cancelTokenSource.token);

		return this;
	};

	cancel = async () : Promise<UploadJob> => {
		if (UploadQueue.getInstance().jobRunning(this.data.id)) {
			this.cancelTokenSource.cancel("Canceled upload job");

			this.canceled = true;

			await UploadQueue.getInstance().finishJob(this);
		}
		return this;
	}
}

export default class UploadQueue {
	private readonly QUEUE_SIZE = 2;
	private queue : (UploadJob | null)[] = new Array(this.QUEUE_SIZE).fill(null);
	private waiting : UploadJob[] = [];
	private finished : UploadJob[] = [];
	private static instance : UploadQueue | null = null;

	private constructor() {};

	static getInstance = () : UploadQueue => {
		if (!UploadQueue.instance) UploadQueue.instance = new UploadQueue();

		return UploadQueue.instance;
	};

	createJob = (item : UploadItem) => new UploadJob(item);

	add = (job: UploadJob, index? : number) => {
		let added = false;

		const addJob = (index : number) => {
			if (job && job.data) {
				this.waiting.splice(0, 1);

				this.queue[index] = job;

				added = true;

				job.start().then(this.finishJob);
			}
		};

		if (index && !(index < 0) && !this.queue[index]) {
			addJob(index);
		} else {
			for (let i = 0; i < this.QUEUE_SIZE; i++) {
				if (!this.queue[i]) {
					addJob(i);
					break;
				}
			}
		}
		if (!added) this.addWaiting(job);
	};

	private addWaiting = (job : UploadJob) => {
		const duplicate = this.waiting.filter(w => w.data.file.name === job.data.file.name)[0];

		!duplicate && this.waiting.push(job);
	};

	finishJob = async (job : UploadJob) => {
		const index = await this.moveToFinished(job);

		if (this.waiting.length > 0) {
			const next = this.waiting[0];

			if (index)
				this.add(next, index);
			else
				this.add(next);
		}
	};

	private moveToFinished = async (job : UploadJob) : Promise<number | undefined> => {
		let index = this.queue.findIndex(j => j && j.data.id === job.data.id);
		const move = () => this.finished.push(job);

		if (index > -1) {
			move();

			this.queue[index] = null;
		} else {
			index = this.waiting.findIndex(j => j === job);

			if (index && !(index < 0)) {
				move();

				this.waiting.splice(index, 1);
			}
		}
		return index;
	};

	suspendUpload = async (upload : UploadItem) => {
		const job = this.queue.concat(this.waiting).filter(j => j && j.data.id === upload.id)[0];

		job && await job.cancel();
	};

	suspendAllJobs = async () => {
		const jobs = this.queue.concat(this.waiting);

		for (let i = 0; i < jobs.length; i++) {
			const job = jobs[i];

			job && await job.cancel();
		}
	};

	selfDestroy = () => this.suspendAllJobs().then(() => UploadQueue.instance = null);

	jobRunning = (id : string) : boolean => this.queue.some(j => j && j.data.id === id);

	jobCanceled = (id : string) : boolean => {
		const job = this.finished.filter(j => j && j.data.id === id)[0];

		return job ? job.canceled : false;
	};
}
