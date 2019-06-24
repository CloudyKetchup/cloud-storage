import React from 'react'

import { Link } from 'react-router-dom'; 

const CreateFolderDialog = props => (

	<div className="standard-dialog">
		<div className="dialog-header">
			<Link to="">
				<button
					className="prev-button"
				>
					<i className="fas fa-chevron-left"/>
				</button>
			</Link>
			<i className="fas fa-folder"/>
			<span>Create new Folder</span>
		</div>
		<div 
			className="container"
			style={{ textAlign : 'center' }}
		>
			<div className="input-field col s6">
				<input 
					placeholder="Name"
					id="folder-name"
					type="text"
				/>
			</div>
			<button 
				className="ok-button"
				onClick={() => props.sendFolder(document.getElementById('folder-name').value)}
			>OK</button>
		</div>
	</div>
);

export default CreateFolderDialog;