import React from 'react';

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
						style={{ width: `${props.parent.state[`uploadingFile${props.name}progress`]}%` }}>
					</div>
				</div>
			</div>
		</div>
	</div>
);

const FileUploadManager = props =>(

	<div className="upload-monitor">
		<div className="upload-monitor-header">

		</div>
		<div className="upload-monitor-content">
			{props.children}
		</div>
	</div>
);

export default FileUploadManager;