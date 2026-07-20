import { useState, useEffect } from "react";
import axios from "axios";
import { NavLink, useNavigate, useParams } from "react-router-dom";
import { useRef } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function Biodata() {
  const [action, setAction] = useState("Submit");
  const { id } = useParams();
  const navigate = useNavigate();
  const formRef = useRef();
  const [errors, setErrors] = useState({});

  const [states, setStates] = useState([]);
  const [cities, setCities] = useState([]);
  useEffect(() => {
    axios.get("http://localhost:8080/api/check-session", {
      withCredentials: true
    })
      .then(() => {
        // session valid
      })
      .catch(() => {
        navigate("/login");
      });
  }, [navigate]);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    dob: '',
    age: '',
    gender: '',
    address: '',
    city: cities.length > 0 ? cities[0].name : '',
    state: states.length > 0 ? states[0].name : '',
    country: 'India',
    skills: [],
    password: '',
    role: 'USER'
  });
  useEffect(() => {
    if (formData.country && formData.state) {
      axios.post("https://countriesnow.space/api/v0.1/countries/state/cities", {
        country: formData.country,
        state: formData.state
      },{withCredentials:false})
        .then(res => {
          setCities([...res.data.data]);
          console.log(res.data.data);
        })
        .catch(err => console.error(err));
    }
  }, [formData.state]);

  useEffect(() => {
    if (formData.country) {
      axios.post("https://countriesnow.space/api/v0.1/countries/states", {
        country: formData.country
      },{withCredentials: false})
        .then(res => {
          setStates(res.data.data.states);
          console.log(res.data.data);
          setCities([]);
        })
        .catch(err => console.error(err));
    }
  }, [formData.country]);



  //  validation function
  function validate() {
    let newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Name is required";
    }

    if (!formData.email) {
      newErrors.email = "Email is required";
    } else if (!/^[A-Za-z0-9+_.-]+@(.+)$/.test(formData.email)) {
      newErrors.email = "Invalid email format";
    }

    if (!formData.phone) {
      newErrors.phone = "Phone is required";
    } else if (!/^[0-9]{10}$/.test(formData.phone)) {
      newErrors.phone = "Phone must be 10 digits";
    }

    if (!formData.dob) {
      newErrors.dob = "Date of birth is required";
    }

    if (!formData.age) {
      newErrors.age = "Age is required";
    }

    if (!formData.gender) {
      newErrors.gender = "Select gender";
    }

    if (!formData.address.trim()) {
      newErrors.address = "Address is required";
    }

    if (!formData.city.trim()) {
      newErrors.city = "City is required";
    }

    if (!formData.state.trim()) {
      newErrors.state = "State is required";
    }

    if (formData.skills.length === 0) {
      newErrors.skills = "Select at least one skill";
    }
    if (action === "Submit") {
      if (!formData.password) {
        newErrors.password = "Password is required";
      } else if (formData.password.length < 6) {
        newErrors.password = "Password must be at least 6 characters";
      }
    } else {
      if (formData.password && formData.password.length < 6) {
        newErrors.password = "Password must be at least 6 characters";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  //  handle change
  function handleChange(event) {
    const { name, value, type, checked } = event.target;

    // clear error while typing

    setErrors({
      ...errors,
      [name]: ""
    });

    if (type === "checkbox") {
      if (checked) {
        setFormData({
          ...formData,
          skills: [...formData.skills, value]
        });
        setErrors(prev => ({
          ...prev, skills: ""
        }));
      } else {
        setFormData({
          ...formData,
          skills: formData.skills.filter(skill => skill !== value)
        });
      }
    } else if (name === "phone" || name === "age") {
      const numericValue = value.replace(/[^0-9]/g, '');
      setFormData({
        ...formData,
        [name]: numericValue
      });
    } else if (type === "text" && (name === "name" || name === "city" || name === "state")) {
      const cleanValue = value.replace(/[^a-zA-Z\s]/g, '').trimStart();
      setFormData({
        ...formData,
        [name]: cleanValue
      });
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  }

  // fetch if id exists and prefill the form
  useEffect(() => {
    if (id) {
      axios.get(`http://localhost:8080/api/biodata/${id}`)
        .then(res => {
          const data = res.data;
          setAction("Update");
          console.log(data);
          setFormData({
            name: data.name || '',
            email: data.email || '',
            phone: data.phone || '',
            dob: data.dob ? data.dob.split("T")[0] : '',
            age: data.age || '',
            gender: data.gender || '',
            address: data.address || '',
            city: data.city || '',
            state: data.state || '',
            country: data.country || 'India',
            skills: Array.isArray(data.skills)
              ? data.skills
              : data.skills ? data.skills.split(",") : [],
            password: '',
            role: data.role || 'USER'
          });
        })
        .catch(err => console.error(err));
    }
  }, [id]);

  function handleKeyDown(e) {
    if (e.key !== "Enter") return;
    e.preventDefault();

    const elements = Array.from(formRef.current.elements).filter(el => el.tagName === "INPUT" || el.tagName === "SELECT" || el.tagName === "TEXTAREA");

    const index = Array.from(elements).indexOf(e.target);

    if (index > -1 && index < elements.length - 1) {
      elements[index + 1].focus();
    }
  }
  // cancel
  const handleCancel = () => {
    toast.info("Edit cancelled");
    setTimeout(() => {
      navigate("/biodata-list");
    }, 1000);
  };
  // submit
  async function handleSubmit(event) {
    event.preventDefault();

    if (!validate()) {
      toast.error("Please fix the errors before submitting");
      return;
    }

    try {
      if (id) {
        let updateData = { ...formData };

        // If password is empty, don't include it in the update
        if (!formData.password) {
          delete updateData.password;
        }

        const response = await axios.put(`http://localhost:8080/api/biodata/${id}`, updateData, { withCredentials: true });
        toast.success("Biodata updated successfully!" + response.data);

      } else {
        const response = await axios.post(
          "http://localhost:8080/api/submit",
          formData,{withCredentials:true}
        );
        toast.success(response.data);
      }
      setTimeout(() => {
        setFormData({
          name: '',
          email: '',
          phone: '',
          dob: '',
          age: '',
          gender: '',
          address: '',
          city: '',
          state: '',
          country: 'India',
          skills: [],
          password: '',
          role: 'USER'
        });
      }, 500);
      setTimeout(() => {
        navigate("/biodata-list");
      }, 1500);

    } catch (error) {
      console.error("Error submitting form:", error);

      if (error.response && error.response.data) {
        toast.error(error.response.data);
      }
      else {
        toast.error("Something went wrong.....Try Again...");
      }
    }
  }

  return (
    <div className="container">

      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} closeOnClick pauseOnHover draggable theme="colored"
      />      <div className="card">
        <h2>Biodata Form</h2>

        <form className="form-div" onSubmit={handleSubmit} ref={formRef}>

          {/* name */}
          <div className="form-group">
            <label>Name</label>
            <input type="text" name="name" value={formData.name} onChange={handleChange} onKeyDown={handleKeyDown} />
            {errors.name && <p className="error">{errors.name}</p>}
          </div>

          {/* email & phone */}
          <div className="row">
            <div className="form-group">
              <label>Email</label>
              <input type="text" name="email" value={formData.email} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.email && <p className="error">{errors.email}</p>}
            </div>

            <div className="form-group">
              <label>Phone</label>
              <input type="text" name="phone" value={formData.phone} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.phone && <p className="error">{errors.phone}</p>}
            </div>
          </div>

          {/* dob & age */}
          <div className="row">
            <div className="form-group">
              <label>DOB</label>
              <input type="date" name="dob" value={formData.dob} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.dob && <p className="error">{errors.dob}</p>}
            </div>

            <div className="form-group">
              <label>Age</label>
              <input type="text" name="age" value={formData.age} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.age && <p className="error">{errors.age}</p>}
            </div>
          </div>

          {/* gender */}
          <div className="form-group">
            <label>Gender</label>
            <div>
              <input type="radio" name="gender" value="male" checked={formData.gender === "male"} onChange={handleChange} /> Male
              <input type="radio" name="gender" value="female" checked={formData.gender === "female"} onChange={handleChange} /> Female
              <input type="radio" name="gender" value="other" checked={formData.gender === "other"} onChange={handleChange} /> Other
            </div>
            {errors.gender && <p className="error">{errors.gender}</p>}
          </div>

          {/* address */}
          <div className="form-group">
            <label>Address</label>
            <textarea name="address" value={formData.address} onChange={handleChange} style={{ resize: "none" }}></textarea>
            {errors.address && <p className="error">{errors.address}</p>}
          </div>

          {/* city / state / country */}
          <div className="row">
            {/* <div className="form-group">
              <label>City</label>
              <input type="text" name="city" value={formData.city} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.city && <p className="error">{errors.city}</p>}
            </div> */}
            <div className="form-group">
              <label>City</label>
              <select name="city" value={formData.city} onChange={handleChange} onKeyDown={handleKeyDown}>
                <option value="">Select city</option>
                {cities.map((city, index) => (
                  <option key={index} value={city}>{city}</option>
                ))}
              </select>
              {errors.city && <p className="error">{errors.city}</p>}
            </div>

            {/* <div className="form-group">
              <label>State</label>
              <input type="text" name="state" value={formData.state} onChange={handleChange} onKeyDown={handleKeyDown} />
              {errors.state && <p className="error">{errors.state}</p>}
            </div> */}
            <div className="form-group">
              <label>State</label>
              <select name="state" value={formData.state} onChange={handleChange} onKeyDown={handleKeyDown}>
                <option value="">Select state</option>
                {states.map((state, index) => (
                  <option key={index} value={state.name}>{state.name}</option>
                ))}
              </select>
              {errors.state && <p className="error">{errors.state}</p>}
            </div>

            <div className="form-group">
              <label>Country</label>
              <select name="country" value={formData.country} onChange={handleChange} onKeyDown={handleKeyDown}>
                <option value="India">India</option>
                <option value="Sri lanka">Srilanka</option>
                <option value="Japan">Japan</option>
              </select>
            </div>
          </div>

          {/* skill */}
          <div className="form-group">
            <label>Skills</label>
            <div>
              <input type="checkbox" value="HTML" checked={formData.skills.includes("HTML")} onChange={handleChange} /> HTML
              <input type="checkbox" value="CSS" checked={formData.skills.includes("CSS")} onChange={handleChange} /> CSS
              <input type="checkbox" value="JavaScript" checked={formData.skills.includes("JavaScript")} onChange={handleChange} /> JavaScript
            </div>
            {errors.skills && <p className="error">{errors.skills}</p>}
          </div>
          <div className="row">
            <label>Password</label>
            <input type="password" name="password" placeholder={action === "Submit" ? "Enter password" : "Enter new password or leave blank to keep the old password"} value={formData.password || ''} onChange={handleChange} onKeyDown={handleKeyDown} />
          </div>
          {errors.password && <p className="error">{errors.password}</p>}

          <div className="form-group">
            <label>Role</label>
            <select name="role" value={formData.role} onChange={handleChange} onKeyDown={handleKeyDown}>
              <option value="USER">User</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div className="form-group">
            {action === "Update" && (
              <button
                type="button"
                onClick={handleCancel}
                style={{ margin: 10 }}
              >
                Cancel
              </button>
            )}

            <button type="submit" className={`btn ${action === 'Submit' ? 'btn-success' : 'update-btn'}`}>{action}</button>
          </div>

          {action === "Submit" &&
            <NavLink to="/biodata-list">
              <button type="button" className="btn btn-outline-success">View Table</button>
            </NavLink>
          }
        </form>
      </div>
    </div>
  );
}

export default Biodata;