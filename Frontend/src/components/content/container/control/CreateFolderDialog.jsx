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
		<div className="dialog-input-container">
			<input 
				placeholder="Name"
				autoComplete="off"
				id="folder-name"
				type="text"
			/>
			<button className="ok-button">
				<Link onClick={() => props.sendFolder(document.getElementById('folder-name').value)} to={'/'}>
					OK
				</Link>
			</button>
		</div>
	</div>
);

export default CreateFolderDialog;
