import { FolderEntity } from '../../../model/entity/FolderEntity';

import { FileEntity } from '../../../model/entity/FileEntity';

import React, { Component }   from "react";
import { ITrashItem }         from "../TrashViewContainer";
import { AppContentContext }  from "../../../App";

import TrashAnimatedButton   	from "../ControlButton/TrashAnimatedButton";
import { Entity }               from "../../../model/entity/Entity";
import { EntityType }           from "../../../model/entity/EntityType";
import { APIHelpers as API, ContentHelpers } from "../../../helpers";

import "./trash-item.css";

type IProps = {
	data: Entity,
	onClick: (...args: any) => void
};

export default class TrashItem extends Component<IProps> implements ITrashItem {

	onDelete = async () => {
		const result = await API.deleteFromTrash(this.props.data);

		result === "OK" && await ContentHelpers.updateTrash();
	};

	onRestore = async () => {
		await API.restoreFromTrash(this.props.data.id) === "OK"
		?
		ContentHelpers.updateTrash()
		:
		API.errorNotification(`Error restoring ${this.props.data.name}`);
	};

	shortenName = () : string => {
		const name = this.props.data.name;

		return name.length > 33 ? name.slice(30) : name ;
	};

	render = () => (
		<div className="trash-file-item">
			<div className="trash-file-main" onClick={() => this.props.onClick(this.props.data.id)}>
				<div style={{ width : "10%", fontSize : 20, textAlign : "center" }}>
					<i style={{ lineHeight : "45px" }} className={this.props.data.type === EntityType.FILE ? "fas fa-file" : "fas fa-folder"}/>
				</div>
				<div style={{ marginLeft : 2, marginTop : 5 ,height : "80%", width : 1, background : "gray" }}/>
				<div style={{ marginLeft : 10, lineHeight : "45px" }}>
					<span>{this.shortenName()}</span>
				</div>
			</div>
			<div className="trash-file-control">
				{
					new TrashAnimatedButton.Builder()
						.icon(<i className="fas fa-times"/>)
						.isHoverable()
						.color("white")
						.style({
							hoverHeight     : "7px",
							hoverColor      : "#EB4034",
							backgroundStyle : { background: "#181818" },
							foregroundStyle : { background: "white", width: "100%", height: "100%" }
						})
						.onClick(this.onDelete)
						.build()
				}
				{
					new TrashAnimatedButton.Builder()
						.icon(<i className="fas fa-undo"/>)
						.isHoverable()
						.color("white")
						.style({
							hoverHeight     : "7px",
							hoverColor      : "#6DC99B",
							backgroundStyle : { background: "#181818" },
							foregroundStyle : { background: "white", width: "100%", height: "100%" }
						})
						.onClick(this.onRestore)
						.build()
				}
			</div>
		</div>
	);
}
