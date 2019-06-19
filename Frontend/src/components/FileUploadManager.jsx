import React, { Component } from 'react';
import axios from "axios";

export const UploadFile = props =>(

	<div className="upload-file">
		<div className="upload-file-icon">
			<span>{props.extension}</span>
		</div>
		<div className="upload-file-info">
			<div>
				<span>{props.name}</span>
			</div>
			<div className="upload-file-progress">
				<div className="progress-bar">
					<div
						className="progress-bar-line"
						style={{width: `${props.progress}%`}}>
					</div>
				</div>
			</div>
		</div>
	</div>
);

export default class FileUploadManage extends Component {
	state = {};

	render() {
		return (
			<div className="upload-monitor">
				<div className="upload-monitor-header">

				</div>
				<div className="upload-monitor-content">
					{this.props.children}
				</div>
			</div>
		);
	}
};
