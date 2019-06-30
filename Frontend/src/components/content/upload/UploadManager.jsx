import React, { Component } from 'react';

export const UploadFile = props => (

	<div className="upload-file">
		<div className="upload-file-icon">
			<span style={{ lineHeight : '40px' }}>{props.data.name.split('.').pop()}</span>
			<div style={{
				height: '40px',
				width : '1px',
				float : 'right',
				background : 'grey'
			}}/>
		</div>
		<div className="upload-file-info">
			<div style={{ height : '40px' }}>
				<span style={{
					lineHeight : '40px',
					marginLeft : '10px'
				}}>{props.data.name.length > 30 ? `${props.data.name.substring(0, 29)}...` : props.data.name }</span>
			</div>
			<div className="upload-file-progress">
				<div className="progress-bar" style={{
					position : 'unset',
					right: '10px',
					width: '100%'
				}}>
					<div
						className="progress-bar-line"
						style={{ width: `${props.parent.state[`uploadingFile${props.data.name}progress`]}%` }}>
					</div>
				</div>
			</div>
		</div>
	</div>
);

export default class FileUploadManager extends Component {
	state = { hide : false }

	render() {
		return (
			<div className="upload-monitor">
				<div className="upload-monitor-header">
					<div className="upload-monitor-title">
						<span>Files Upload</span>
					</div>
					<div className="upload-monitor-control" onClick={this.props.onClose} style={{ right : 0 }}>
						<i className="fas fa-times"/>
					</div>
					<div className="upload-monitor-control" onClick={() => this.setState({ hide : !this.state.hide })} style={{ right : '40px' }}>
						<i className={this.state.hide ? "fas fa-sort-up" : "fas fa-sort-down"}/>
					</div>
				</div>
				<div className="upload-monitor-content" style={{ display : this.state.hide ? 'none' : 'grid' }}>
					{this.props.children}
				</div>
			</div>
		);
	}
}