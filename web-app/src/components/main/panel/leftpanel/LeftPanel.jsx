import React, { Component } from 'react'
import { Link } from 'react-router-dom';

export default class LeftPanel extends Component {

	componentDidMount() {
		const trashButton = document.getElementsByClassName("trash-button")[0];

		if (trashButton !== null) {
			trashButton.addEventListener("mouseover", e => {
				const buttonHoverLine = document.getElementById("trash-button-hover-line");

				if (buttonHoverLine !== null) {
					trashButton.style.color = "rgb(243, 44, 44)";
					buttonHoverLine.style.width = "100%";
				}
			});
			trashButton.addEventListener("mouseout", e => {
				const buttonHoverLine = document.getElementById("trash-button-hover-line");

				if (buttonHoverLine !== null) {
					trashButton.style.color = "unset";
					buttonHoverLine.style.width = 0;
				}
			});
		}
	}

	render = () => (
		<div className="side-panel">
			<div className="side-panel-content">
				{this.props.folderInfo !== undefined
					&&
					<div className="element-info">
						<div>
							<span className="description-text">Location</span>
							<span className="element-info-text">{this.props.folderInfo.name}</span>
						</div>
						<div>
							<span className="description-text">Folders</span>
							<span className="element-info-text">{this.props.folders}</span>
						</div>
						<div>
							<span className="description-text">Files</span>
							<span className="element-info-text">{this.props.files}</span>
						</div>
						<div>
							<span className="description-text">Size</span>
							<span className="element-info-text">{this.props.folderInfo.size}</span>
						</div>
					</div>}
				<Link to={"/trash"}>
					<div className="trash-button">
						<span>Trash</span>
						<div style={{ height: 2, width: "100%", background: "white", marginTop: 5 }}>
							<div id="trash-button-hover-line" style={{ background: "#F32C2C", width: 0, height: 2 }}></div>
						</div>
					</div>
				</Link>
				<div className="memory-info">
					<div className="used-memory-info">
						<span className="used-memory-text">
							{this.props.memory.total - this.props.memory.free} / {this.props.memory.total} GB
					</span>
					</div>
					<div className="progress-bar">
						<div
							className="progress-bar-line"
							style={{ width: `${(this.props.memory.total - this.props.memory.free) / this.props.memory.total * 100}%` }}>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}