import React, { Component, FC } from "react";

import { Entity }               from "../../model/entity/Entity";
import { EntityType }           from "../../model/entity/EntityType";
import { AppContentContext }    from "../../App";
import TrashItem                from "./TrashItem/TrashItem";
import TrashAnimatedButton      from "./ControlButton/TrashAnimatedButton";
import { APIHelpers as API, ContentHelpers }                from "../../helpers";
import TrashItemInfoContainer, { TrashEmptyInfoContainer }  from "./InfoContainer/TrashItemInfoContainer";

import "./trash-view-container.css";

export interface ITrashItem {
	onDelete    : (...args : any) => any
}

interface IState {
	items       : Entity[]
	selectedItem: Entity | null
	rightPanel  : boolean
}

const TrashItemsList : FC<{ items : Entity[], onItemClick : (...args : any) => void }> = props => {
	const files = props.items.filter(i => i.type === EntityType.FILE);
	const folders = props.items.filter(i => i.type === EntityType.FOLDER);

	return (
		<div>
			{
				folders.length > 0
				&&
                <div className="list-items-separator">
                    <span>Folders</span>
                </div>
			}
			{
				folders.length > 0
				&&
                <div className="trash-items-list">
					{folders.map(data => <TrashItem key={data.id} onClick={props.onItemClick} data={data}/>)}
                </div>
			}
			{
				files.length > 0
				&&
                <div className="list-items-separator">
                    <span>Files</span>
                </div>
			}
			{
				files.length > 0
				&&
                <div className="trash-items-list">
					{files.map(data => <TrashItem key={data.id} onClick={props.onItemClick} data={data}/>)}
                </div>
			}
		</div>
	);
};

const EmptyContentList : FC = () => (
	<div>
		<div style={{ margin : "auto", textAlign : "center", padding : "30px" }}>
			<img
				style={{ width : "40%" }}
				src="https://www.pngkey.com/png/full/315-3152007_png-animuthinku-thinking-meme-face-anime.png"
				alt={"..."}/>
			<div style={{ marginTop : "20px", fontSize : 20 }}>
				<span>Nothing here</span>
			</div>
		</div>
	</div>
);

export default class TrashViewContainer extends Component {
	state : IState = {
		items : [],
		selectedItem : null,
		rightPanel : false
	};

	ref : HTMLElement | null = null;

	componentDidMount = () => {
		this.ref = document.getElementsByClassName("trash-view-container")[0] as HTMLElement;

		this.slideDown();

		setTimeout(async () => {
			ContentHelpers.updateTrash();

			this.setState({ rightPanel : true });
		}, 300);
	};

	componentDidUpdate = () => {
		this.state.items !== AppContentContext.trashItems
		&&
		this.setState({ items: AppContentContext.trashItems });
	};

	shouldComponentUpdate = (prevState : IState, nextState : IState) =>  prevState.items !== nextState.items;

	deleteAll = async () => {
		const result = await API.emptyTrash();

		ContentHelpers.updateTrash();

		result !== "OK" && API.errorNotification("Couldn't empty trash");
	};

	restoreAll = async () => {
		const result = await API.restoreAllFromTrash();

		ContentHelpers.updateTrash();

		result !== "OK" && API.errorNotification("Couldn't restore all items from trash");
	};

	onItemClick = (id : string) => {
		const item = this.state.items.filter(i => i.id === id)[0];

		this.state.selectedItem !== item
		&&
		this.setState({ selectedItem : item });
	};

	slideDown = async () => {
		setTimeout(() => {
			if (this.ref) {
				this.ref.style.opacity = "1";
				this.ref.style.top = "0";
			}
		}, 100);
	};

	render = () => (
		<div className="trash-view-container">
			<div className="flex-container">
				{
					this.state.items.length > 0
						?
						<div className="flex-columns">
							<div className="flex-container-left">
								<TrashItemsList onItemClick={this.onItemClick} items={this.state.items} />
							</div>
							<div style={{ marginTop: 30, height: "35em", width: 1.5, background: "#181818" }}/>
							<div className="flex-container-right">
								{
									this.state.selectedItem
									&&
									this.state.items.filter(i => this.state.selectedItem && i.id === this.state.selectedItem.id)[0]
										?
										<TrashItemInfoContainer data={this.state.selectedItem} />
										:
										this.state.rightPanel
										&&
                                        <TrashEmptyInfoContainer />
								}
							</div>
						</div>
						:
						<EmptyContentList/>
				}
				<div className="flex-container-footer">
					{
						new TrashAnimatedButton.Builder()
							.color("#181818")
							.icon(<i className="fas fa-chevron-left" />)
							.title("Go back")
							.isLink("/")
							.style({
								hoverColor: "gray",
								hoverHeight: "15px",
								foregroundStyle : { textAlign: "center", background: "#181818", transform: "translateY(-8px)" },
								backgroundStyle : { float : "left" }
							})
							.build()
					}
					{
						this.state.items.length > 0
						&&
                        <div style={{ display: "flex" }}>
							{
								new TrashAnimatedButton.Builder()
									.color("#181818")
									.icon(<i className="far fa-trash-alt" />)
									.title("Delete all")
									.style({
										hoverColor: "#EB4034",
										foregroundStyle: { transform: "translateY(-16px)" },
										backgroundStyle: { background: "#181818" }
									})
									.isDisabled(this.state.items.length === 0)
									.onClick(this.deleteAll)
									.build()
							}
							{
								new TrashAnimatedButton.Builder()
									.color("#181818")
									.icon(<i className="fas fa-trash-restore" />)
									.title("Restore all")
									.style({
										hoverColor: "#6DC99B",
										foregroundStyle: { transform: "translateY(-16px)" },
										backgroundStyle: { background: "#181818" }
									})
									.isDisabled(this.state.items.length === 0)
									.onClick(this.restoreAll)
									.build()
							}
                        </div>
					}
				</div>
			</div>
		</div>
	);
}
