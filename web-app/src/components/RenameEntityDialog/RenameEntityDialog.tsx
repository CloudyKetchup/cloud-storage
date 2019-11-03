import React, { Component } from 'react';

import { Link, match } 	from 'react-router-dom';
import { FileEntity } 	from '../../model/entity/FileEntity';
import { FolderEntity } from '../../model/entity/FolderEntity';
import { APIHelpers } 	from '../../helpers';
import { Entity } 		from '../../model/entity/Entity';

interface IProps {
	onRename : (target : Entity, newName : string) => void
	match : match<{ id : string, type : string }>
}

interface IState { data : FileEntity | FolderEntity | null}

export default class RenameEntityDialog extends Component<IProps, IState> {
	state : IState = { data : null }

	componentDidMount() {
		const entityType = this.props.match.params.type;
		const functionName = `get${entityType[0].toUpperCase() + entityType.slice(1)}Data`;
		
		if (functionName === "getFileData" || functionName === "getFolderData")
			APIHelpers[functionName](this.props.match.params.id)
				.then(result => {
					if ((result as FileEntity) !== undefined)
						this.setState({ data : result as FileEntity });
					else if ((result as FolderEntity) !== undefined)
						this.setState({ data : result as FolderEntity });
				})
	}

	render = () => (
		<div className="standard-dialog">
			<div className="dialog-header">
				<Link to="/">
					<button
						className="prev-button"
					>
						<i className="fas fa-chevron-left"/>
					</button>
				</Link>
				<i className="fas fa-folder"/>
				<span>Rename {this.state.data && this.state.data.name}</span>
			</div>
			<div className="dialog-input-container">
				<input
					defaultValue={`${this.state.data && this.state.data.name}`}
					autoComplete="off"
					placeholder="Name"
					id="folder-name"
					type="text"
					style={{
						margin: 'auto',
						marginTop: '20px',
						borderBottom: 'solid 1px grey',
						height: '30px',
						width: '90%'
					}}
				/>
				<button className="ok-button">
					<Link  onClick={() => {
							const field = document.getElementById('folder-name') as HTMLInputElement
							
							if (field && this.state.data) this.props.onRename(this.state.data, field.value)
						}} to={'/'}>
						Rename
					</Link>
				</button>
			</div>
		</div>
	);
}
