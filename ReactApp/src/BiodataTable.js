import { useState } from "react";
import "./styles/BiodataTable.css";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import axios from "axios";
import { FaEdit, FaTrash } from "react-icons/fa";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function BiodataTable() {
  axios.defaults.withCredentials = true;
  const [data, setData] = useState([]);
  const [search, setSearch] = useState("");
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [loading, setLoading] = useState(false);

  const [selected, setSelected] = useState(false);
  const [selectedIds, setSelectedIds] = useState([]);
  const [reload, setReload] = useState(false);

  //select all functionality states
  const [selectAllCurrent, setSelectAllCurrent] = useState(false);
  const [selectAllGlobal, setSelectAllGlobal] = useState(false);

  const [totalElements, setTotalElements] = useState(0);
  const [userName, setUserName] = useState("");
  //  Row click handler
  const handleRowClick = (id) => {
    if (!selected) return;

    if (selectAllGlobal) return;

    setSelectedIds((prev) =>
      prev.includes(id)
        ? prev.filter((item) => item !== id)
        : [...prev, id]
    );
  };
  // Sync select all checkbox with individual selections
  useEffect(() => {
    if (selectAllGlobal) {
      setSelectAllCurrent(true);
      return;
    }

    if (data.length === 0) {
      setSelectAllCurrent(false);
      return;
    }

    const currentPageIds = data.map(item => item.id);

    const allSelectedInPage = currentPageIds.every(id =>
      selectedIds.includes(id)
    );

    setSelectAllCurrent(allSelectedInPage);

  }, [data, selectedIds, selectAllGlobal]);

  // check session on component mount
  useEffect(() => {
    const checkSession = async () => {
      try {
        const res = await axios.get(
          "http://localhost:8080/api/check-session",
          { withCredentials: true }
        );

        setUserName(res.data.name);

      } catch (error) {
        console.log("Session invalid");

        // delay navigation slightly (prevents unsafe navigation error)
        setTimeout(() => {
          navigate("/login", { replace: true });
        }, 0);
      }
    };

    checkSession();
  }, [navigate]);


  // Confirm delete or edit action
  const handleConfirm = async (actionType) => {

    if (actionType === "delete") {
      if (selectAllGlobal) {
        if (!window.confirm("Are you sure you want to delete ALL records?")) return;

        try {
          await axios.delete("http://localhost:8080/api/biodata/delete-all", { withCredentials: true });

          toast.success("All biodata deleted successfully!");

          setReload(prev => !prev);
          setSelected(false);
          setSelectedIds([]);
          setSelectAllGlobal(false);
          setSelectAllCurrent(false);
          setCurrentPage(0);

        } catch (error) {
          console.error("Error deleting all data:", error);
          toast.error("Failed to delete all records.");
        }

        return;
      }
      if (selectedIds.length === 0) {
        toast.error("Please select at least one entry to delete.");
        return;
      }

      if (!window.confirm(`Are you sure you want to delete ${selectedIds.length} entries?`)) return;

      try {
        await Promise.all(
          selectedIds.map(id =>
            axios.delete(`http://localhost:8080/api/biodata/${id}`, { withCredentials: true })
          )
        );

        toast.success("Selected biodata deleted successfully!");

        // remove from dom
        setData(prev => prev.filter(item => !selectedIds.includes(item.id)));

        // reset selection
        setSelected(false);
        setSelectedIds([]);
        setSelectAllCurrent(false);

        // pagination fix
        if (data.length <= selectedIds.length && currentPage > 0) {
          setCurrentPage(prev => prev - 1);
        }

      } catch (error) {
        console.error("Error deleting data:", error);
        toast.error("Failed to delete selected records.");
      }

    }

    else if (actionType === "edit") {
      if (selectedIds.length !== 1) {
        toast.error("Please select exactly one entry to edit.");
        return;
      }
      console.log(selectedIds[0]);
      navigate(`/edit/${selectedIds[0]}`);
    }
  };

  // Cancel selection
  const handleCancel = () => {
    setSelected(false);
    setSelectedIds([]);
    toast.info("Action cancelled");
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
    }, 500);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {

    const fetchData = async () => {
      try {
        setLoading(true);
        const response = await axios.get(`http://localhost:8080/api/biodata/search?keyword=${encodeURIComponent(debouncedSearch)}&page=${currentPage}&size=4`
          , { withCredentials: true }
        );
        setData(response.data.content);
        setTotalPages(response.data.totalPages);
        setTotalElements(response.data.totalElements);
      } catch (error) {
        console.error("Error searching data:", error);
        if (error.response && error.response.status === 401) {
          toast.error("Session expired. Please login again.");
          navigate("/login", { replace: true });
        } else {
          toast.error("Failed to fetch data.");
        }
      }
      finally {
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      }
    }
    fetchData();
  }, [debouncedSearch, currentPage, reload]);

  // upload csv file
  const uploadCsv = () => {
    const input = document.createElement("input");
    input.type = "file";
    input.accept = ".csv";

    input.onchange = async (e) => {
      const file = e.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append("file", file);

      try {
        await axios.post("http://localhost:8080/api/upload/csv", formData, {
          headers: { "Content-Type": "multipart/form-data" },
          withCredentials: true
        });
        toast.success("CSV uploaded successfully!");
        setReload(prev => !prev);
        setCurrentPage(0);
      } catch (error) {
        console.error("Error uploading CSV:", error);
        toast.error("Failed to upload CSV.");
      }
    };

    input.click();
  };

  // download csv file
  const downloadCSV = async () => {
    const response = await fetch("http://localhost:8080/api/download/csv", { credentials: "include" });

    const blob = await response.blob();

    const url = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = "biodata.csv";

    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  // download selected entries as csv
  const downloadSelectedCsv = async () => {
    if (selectAllGlobal) {
      downloadCSV();
      return;
    }
    const response = await axios.post(
      "http://localhost:8080/api/download/csv/selected",
      selectedIds,
      {
        responseType: "blob",
        credentials: "include"
      }
    );

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "selected_biodata.csv");
    document.body.appendChild(link);
    link.click();
  };

  // download pdf file
  const downloadPDF = async () => {
    const response = await fetch("http://localhost:8080/api/download/pdf", { credentials: "include" });

    const blob = await response.blob();

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "biodata.pdf";
    document.body.appendChild(a);
    a.click();
    a.remove();
  };

  // download selected entries as pdf
  const downloadSelectedPDF = async () => {
    if (selectAllGlobal) {
      downloadPDF();
      return;
    }
    const response = await axios.post(
      "http://localhost:8080/api/download/pdf/selected",
      selectedIds,
      {
        responseType: "blob",
        credentials: "include"
      }
    );

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "selected_biodata.pdf");
    document.body.appendChild(link);
    link.click();
  };

  //Logout handler
  const handleLogout = async () => {
    try {
      // Clear session and role
      sessionStorage.removeItem("userRole");
      sessionStorage.removeItem("userEmail");

      // Optional: notify server about logout
      await axios.post("http://localhost:8080/api/logout", {}, { withCredentials: true });

      // Notify other tabs
      localStorage.setItem("logout", Date.now());

      // Navigate to login
      navigate("/login", { replace: true });
    } catch (error) {
      console.error("Logout failed:", error);
      toast.error("Logout failed. Please try again.");
    }
  };


  return (
    <div className="table-container">
      <div className="header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>


        <h1 style={{ color: "Black" }}> {userName} </h1>
        <button onClick={() => handleLogout()} className="logout-btn">Logout</button>
      </div>

      <ToastContainer position="top-right" autoClose={3000} theme="colored" />

      <h2 className="title">Biodata List</h2>

      <div className="d-flex justify-content-between align-items-center mb-3">

        <div>
          <input
            type="text"
            placeholder="Search by name or city..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value.trimStart());
              setCurrentPage(0);
            }}
            style={{ padding: "8px", width: "300px", marginBottom: "10px" }}
          />
        </div>
        {selectedIds.length > 0 &&
          <div className="d-flex gap-2">
            <button onClick={() => { handleConfirm('edit') }} className="bttn btn-edit">Edit<FaEdit /></button>
            <button onClick={() => { handleConfirm('delete') }} className="bttn btn-delete">Delete<FaTrash /></button>
          </div>}
      </div>

      <div className="d-flex justify-content-between mb-3">
        <div>
          {selectAllCurrent && !selectAllGlobal && (
            <div >
              <span>
                All {data.length} records on this page are selected.
              </span>
              <button
                style={{ marginLeft: "10px", color: "blue", border: "none", background: "none", cursor: "pointer" }}
                onClick={() => {
                  setSelectAllGlobal(true);
                }}
              >
                Select all {totalElements} records
              </button>
            </div>
          )}

          {selectAllGlobal && (
            <div >
              <span>All records are selected.</span>
              <button
                style={{ marginLeft: "10px", color: "red", border: "none", background: "none", cursor: "pointer" }}
                onClick={() => {
                  setSelectedIds([]);
                  setSelectAllCurrent(false);
                  setSelectAllGlobal(false);
                  handleCancel();
                }}
              >
                Clear selection
              </button>
            </div>
          )}
        </div>
        <div className="d-flex gap-2">
          <button onClick={uploadCsv}>Upload CSV To add biodata</button>
          <button onClick={selectedIds.length === 0 ? downloadCSV : downloadSelectedCsv} disabled={data.length === 0}>
            Download CSV
          </button>
          <button onClick={selectedIds.length === 0 ? downloadPDF : downloadSelectedPDF} disabled={data.length === 0} style={{ marginLeft: "10px" }}>
            Download PDF
          </button>
        </div>
      </div>

      <div className="table-wrapper">
        <table className="styled-table">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  checked={selectAllGlobal || selectAllCurrent}
                  onChange={(e) => {
                    const checked = e.target.checked;
                    const currentPageIds = data.map(item => item.id);

                    if (checked) {
                      setSelectedIds((prev) => [
                        ...new Set([...prev, ...currentPageIds])
                      ]);
                    } else {
                      setSelectedIds((prev) =>
                        prev.filter(id => !currentPageIds.includes(id))
                      );

                      setSelectAllGlobal(false);
                    }
                  }}
                />
              </th>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>DOB</th>
              <th>Age</th>
              <th>Gender</th>
              <th>Address</th>
              <th>City</th>
              <th>State</th>
              <th>Country</th>
              <th>Skills</th>
            </tr>
          </thead>

          <tbody>
            {data.length === 0 &&
              <tr>
                <td colSpan="13" style={{ textAlign: "center", padding: "20px" }}>
                  {loading ? "Loading..." : "No matching results"}
                </td>
              </tr>
            }

            {data.map((item) => (
              <tr
                key={item.id}
                onClick={() => handleRowClick(item.id)}
                className={selectAllGlobal || selectedIds.includes(item.id) ? "selected-row" : ""}
              >
                <td>
                  <input
                    type="checkbox"
                    checked={selectAllGlobal || selectedIds.includes(item.id)}
                    onChange={(e) => {
                      e.stopPropagation();
                      if (selectAllGlobal) return;
                      if (e.target.checked) {
                        setSelectedIds([...selectedIds, item.id]);
                      } else {
                        setSelectedIds(selectedIds.filter((id) => id !== item.id));
                        setSelectAllCurrent(false);
                      }
                    }}
                  />
                </td>
                <td>{item.id}</td>
                <td>{item.name}</td>
                <td>{item.email}</td>
                <td>{item.phone}</td>
                <td>{item.dob}</td>
                <td>{item.age}</td>
                <td>{item.gender}</td>
                <td>{item.address}</td>
                <td>{item.city}</td>
                <td>{item.state}</td>
                <td>{item.country}</td>
                <td>{item.skills ? item.skills.join(", ") : ""}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="pagination">
          <button disabled={currentPage === 0} onClick={() => setCurrentPage(currentPage - 1)}>Previous</button>
          <span>Page {totalPages > 0 ? currentPage + 1 : 0} of {totalPages}</span>
          <button disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage(currentPage + 1)}>Next</button>
        </div>
      </div>

      <button className="view-btn" onClick={() => navigate("/biodata")}>
        Add new Biodata
      </button>
    </div>
  );
}

export default BiodataTable;