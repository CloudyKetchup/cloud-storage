import React, { Component } from 'react';

import { Link } 					from 'react-router-dom';
import { APIHelpers as API } 		from '../../helpers';
import { Entity } 					from '../../model/entity/Entity';
import { EntityType } 				from '../../model/entity/EntityType';
import { FileEntity } 				from '../../model/entity/FileEntity';
import { FileExtensionIcons }		from '../File/FileExtensionIcons';
import App, { AppContentContext } 	from '../../App';

type TrashItemProps = {
	data : Entity
	restoreFromTrash : () => void
	deleteFromTrash : () => void
};

class TrashItem extends Component<TrashItemProps> {
	state = { backgroundOverlay : false };

	componentDidMount() {
		const div = document.getElementById(`trash-item-${this.props.data.id}`);

		if (div !== null) {
			div.addEventListener("contextmenu", e => {
				e.preventDefault();

				this.setState({ backgroundOverlay : !this.state.backgroundOverlay });
			})
		}
	}

	icon = () => {
		if (this.props.data.type === EntityType.FILE) {
			const data = this.props.data as FileEntity;

			return <i className={`${FileExtensionIcons[data.extension as any]}`}/>;
		} else if (this.props.data.type === EntityType.FOLDER) {
			return <i className="fas fa-folder"/>;
		}
	};

	name = () => {
		const name = this.props.data.name;

		return name.length > 23 ? `${name.substring(0, 24)}...` : name;
	};

	render = () => (
		<div id={`trash-item-${this.props.data.id}`} className="trash-item">
			<div className="trash-item-background">
				<div className="trash-item-background-control">
					<div onClick={this.props.restoreFromTrash}>
						<i className="fas fa-trash-restore" />
					</div>
					<div onClick={this.props.deleteFromTrash}>
						<i className="fas fa-trash-alt" />
					</div>
				</div>
			</div>
			<div 
				className="trash-item-foreground"
				id={`trash-item-${this.props.data.id}-foreground`}
				style={{ right : this.state.backgroundOverlay ? "80px" : "0" }}>
				<div className="trash-item-icon">
					<i className={
						this.props.data.type === EntityType.FILE
							? FileExtensionIcons[(this.props.data as FileEntity).extension as any]
							: "fas fa-folder"
					} />
				</div>
				<div className="trash-item-name" style={{ padding : 10 }}>
					<span>{this.name()}</span>
				</div>
			</div>
		</div>
	);
}

export default class TrashContainer extends Component<{ app : App }> {

	getItems = () => API.getTrashItems().then(AppContentContext.setTrashItems);

	emptyTrash = async () => {
		if (AppContentContext.trashItems.length > 0)
			API.emptyTrash().then(response => { if (response === "OK") this.getItems(); })
	};

	restoreFromTrash = async (target : Entity) => {
		const result = await API.restoreFromTrash(target);

		if (result === "OK") {
			this.props.app.updateFolder().then(() => this.getItems());
		}
	};

	deleteFromTrash = async (target : Entity) => {
		API.deleteFromTrash(target).then(response => { if (response === "OK") this.getItems()});
	};

	render = () => (
		<div className="trash-container">
			<div className="trash-container-header">
				<div className="trash-container-top-pad"/>
				<Link to={"/"}>
					<button className="trash-container-close-button">
						<i className="fas fa-times"/>
					</button>
				</Link>
			</div>
			<div className="trash-content">
				{AppContentContext.trashItems.map((item: Entity) =>
					<TrashItem
						key={item.id}
						data={item}
						deleteFromTrash={() => this.deleteFromTrash(item)}
						restoreFromTrash={() => this.restoreFromTrash(item)}
					/>)}
			</div>
			<div className="trash-side-buttons">
				<div className="trash-control-button" style={{ background: "#F32C2C" }} onClick={this.emptyTrash}>
					<i className="fas fa-minus-circle trash-control-button-icon"/>
				</div>
				<div className="trash-control-button" style={{ background: "#fafafa" }}>
					<i className="fas fa-info-circle trash-control-button-icon"/>
				</div>
			</div>
		</div>
	);
}
